import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import {
  buildFcmMessage,
  buildNotificationText,
  type DailyNotificationItems,
  type DDayCandidate,
  type RoutineCycle,
  toDailyDDays,
} from "../_shared/daily-notification.ts";
import { FcmClient } from "../_shared/fcm.ts";
import { HttpError, serve } from "../_shared/http.ts";
import { createAdminClient, secretKey } from "../_shared/supabase.ts";

type AdminClient = ReturnType<typeof createAdminClient>;

interface RoutineRow {
  title: string;
  cycle: RoutineCycle;
  unit: string;
  goal_amount: number | null;
  period_total: number;
}

interface DDayCandidateRow {
  title: string;
  base_date: string;
  counts_base_day: boolean;
  repeats: DDayCandidate["repeats"];
  end_date: string | null;
  overflow_policy: DDayCandidate["overflowPolicy"];
}

// 메모, 루틴, 디데이는 종류마다 따로 조회한다. 한 종류의 실패는 그 종류만 null로 두고 다른 종류의 조회를 막지 않는다.
async function fetchDailyItems(client: AdminClient, accountId: string, localDate: string): Promise<DailyNotificationItems> {
  const args = { target_account_id: accountId, target_date: localDate };
  const [memoResult, routineResult, ddayResult] = await Promise.allSettled([
    client.rpc("list_daily_memos", args),
    client.rpc("list_daily_routines", args),
    client.rpc("list_daily_dday_candidates", args),
  ]);

  const unwrap = <T>(name: string, result: PromiseSettledResult<{ data: T | null; error: unknown }>): T | null => {
    if (result.status === "rejected") {
      console.error(`${name} failed`, { account_id: accountId, error: result.reason });
      return null;
    }
    if (result.value.error || result.value.data === null) {
      console.error(`${name} failed`, { account_id: accountId, error: result.value.error });
      return null;
    }
    return result.value.data;
  };

  const memos = unwrap<{ title: string }[]>("list_daily_memos", memoResult);
  const routines = unwrap<RoutineRow[]>("list_daily_routines", routineResult);
  const ddayCandidates = unwrap<DDayCandidateRow[]>("list_daily_dday_candidates", ddayResult);

  let ddays: DailyNotificationItems["ddays"] = null;
  if (ddayCandidates !== null) {
    try {
      ddays = toDailyDDays(
        ddayCandidates.map((row) => ({
          title: row.title,
          baseDate: row.base_date,
          countsBaseDay: row.counts_base_day,
          repeats: row.repeats,
          endDate: row.end_date,
          overflowPolicy: row.overflow_policy,
        })),
        localDate,
      );
    } catch (error) {
      console.error("daily dday resolution failed", { account_id: accountId, error });
    }
  }

  return {
    memoTitles: memos?.map((memo) => memo.title) ?? null,
    routines: routines?.map((row) => ({
      title: row.title,
      cycle: row.cycle,
      unit: row.unit,
      goalAmount: row.goal_amount,
      periodTotal: Number(row.period_total),
    })) ?? null,
    ddays,
  };
}

// pg_cron이 15분마다 secret 키를 apikey 헤더로 보내 호출한다. 앱이나 사용자 세션으로는 호출할 수 없어야 하므로 키가 정확히 일치하는지 본다.
serve(async (request) => {
  if (request.headers.get("apikey") !== secretKey()) {
    throw new HttpError(401, "unauthorized");
  }

  const client = createAdminClient();

  const { error: purgeError } = await client.rpc("purge_stale_fcm_tokens");
  if (purgeError) {
    console.error("purge_stale_fcm_tokens failed", purgeError);
  }

  const { data: dueTokens, error: listError } = await client.rpc("list_due_daily_notification_tokens");
  if (listError) {
    console.error("list_due_daily_notification_tokens failed", listError);
    throw new HttpError(500, "due_tokens_unavailable");
  }

  if (dueTokens.length === 0) {
    return { sent: 0, failed: 0, removed: 0 };
  }

  const fcm = await FcmClient.create();
  let sent = 0;
  let failed = 0;
  let removed = 0;

  for (const due of dueTokens) {
    try {
      const text = buildNotificationText(due.language, await fetchDailyItems(client, due.account_id, due.local_date));
      const target = due.kind === "installation_id" ? { fid: due.token } : { token: due.token };
      const result = await fcm.send(buildFcmMessage(target, text, due.local_date, new Date(due.expires_at), new Date()));

      switch (result.status) {
        case "sent":
          sent++;
          break;
        case "invalid_token": {
          removed++;
          const { error } = due.kind === "installation_id"
            ? await client.rpc("unregister_fcm_installation_id", { installation_id: due.token })
            : await client.rpc("unregister_fcm_token", { token: due.token });
          if (error) {
            console.error("fcm unregistration failed", { kind: due.kind, error });
          }
          break;
        }
        case "failed":
          failed++;
          console.error("fcm send failed", result.error);
          break;
      }
    } catch (error) {
      failed++;
      console.error("daily notification failed", { account_id: due.account_id, error });
    }
  }

  return { sent, failed, removed };
});
