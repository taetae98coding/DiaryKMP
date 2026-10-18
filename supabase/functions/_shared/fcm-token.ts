import { z } from "npm:zod@3";

// 시간대와 언어가 함께 오면 등록, 둘이 비어 있으면 해제다. 게스트는 세션이 없어 해제 요청에 인증을 붙일 수 없다.
// 현재 앱은 설치 ID(installationId)를, 설치 ID를 쓰기 전의 이전 버전 앱은 방식 없이 등록 토큰(token)을 보낸다.
// 두 값을 함께 담은 요청은 어느 방식인지 정할 수 없으므로 거절한다.
const registration = { timeZone: z.string().min(1), language: z.string().min(1) };
const unregistration = { timeZone: z.null().optional(), language: z.null().optional() };
const installationId = { installationId: z.string().min(1), token: z.undefined() };
const token = { token: z.string().min(1), installationId: z.undefined() };

export const fcmTokenSubmitRequestSchema = z.union([
  z.object({ ...installationId, ...registration }),
  z.object({ ...installationId, ...unregistration }),
  z.object({ ...token, ...registration }),
  z.object({ ...token, ...unregistration }),
]);

export type FcmTokenSubmitRequest = z.infer<typeof fcmTokenSubmitRequestSchema>;
