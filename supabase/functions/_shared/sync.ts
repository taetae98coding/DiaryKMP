import { z } from "npm:zod@3";

const entitySchema = {
  id: z.string().uuid(),
  isFinished: z.boolean(),
  isDeleted: z.boolean(),
  updatedAt: z.string().datetime({ offset: true }),
  createdAt: z.string().datetime({ offset: true }),
};

const detailSchema = {
  title: z.string(),
  description: z.string(),
  color: z.number().int(),
};

const localDateTimeSchema = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d{1,9})?)?$/);

export const tagPushRequestSchema = z.object({
  tagList: z.array(
    z.object({
      ...entitySchema,
      detail: z.object({
        ...detailSchema,
        emoji: z.string(),
      }),
    }),
  ),
});

export const memoPushRequestSchema = z.object({
  memoList: z.array(
    z.object({
      ...entitySchema,
      detail: z
        .object({
          ...detailSchema,
          isAllDay: z.boolean().nullable(),
          start: localDateTimeSchema.nullable(),
          endInclusive: localDateTimeSchema.nullable(),
        })
        .refine(
          (detail) =>
            (detail.isAllDay === null) === (detail.start === null) &&
            (detail.isAllDay === null) === (detail.endInclusive === null),
          { message: "isAllDay, start and endInclusive must be set together." },
        )
        .refine(
          (detail) =>
            detail.start === null ||
            detail.endInclusive === null ||
            new Date(detail.start).getTime() <= new Date(detail.endInclusive).getTime(),
          { message: "start must not be after endInclusive." },
        ),
      primaryTagId: z.string().uuid().nullable(),
    }),
  ),
});

export const placePushRequestSchema = z.object({
  placeList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z.object({
        ...detailSchema,
        latitude: z.number().min(-90).max(90),
        longitude: z.number().min(-180).max(180),
        address: z.string().optional(),
      }),
    }),
  ),
});

export const webPushRequestSchema = z.object({
  webList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z.object({
        title: z.string(),
        description: z.string(),
        url: z.string(),
        headerList: z.array(
          z.object({
            name: z.string(),
            value: z.string(),
          }),
        ),
      }),
    }),
  ),
});

export const musicPushRequestSchema = z.object({
  musicList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z.object({
        link: z.string(),
        title: z.string(),
        artist: z.string(),
        thumbnail: z.string(),
      }),
    }),
  ),
});

export const qrPushRequestSchema = z.object({
  qrList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z.object({
        title: z.string(),
        description: z.string(),
        value: z.string(),
      }),
    }),
  ),
});

export const checklistPushRequestSchema = z.object({
  checklistList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z.object({
        title: z.string(),
        description: z.string(),
      }),
    }),
  ),
});

export const checklistItemPushRequestSchema = z.object({
  checklistItemList: z.array(
    z.object({
      id: z.string().uuid(),
      checklistId: z.string().uuid(),
      content: z.string(),
      isChecked: z.boolean(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

const localDateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/);

const ddayRepeatValueSchema = z.number().int().min(1).max(2147483647);

// 간격 하나는 days나 months 중 정확히 하나만 가지므로 다른 키가 섞이지 않도록 strict로 받는다.
const ddayRepeatSchema = z.union([
  z.object({ days: ddayRepeatValueSchema }).strict(),
  z.object({ months: ddayRepeatValueSchema }).strict(),
]);

export const ddayPushRequestSchema = z.object({
  ddayList: z.array(
    z.object({
      id: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z
        .object({
          title: z.string(),
          description: z.string(),
          color: z.number().int(),
          baseDate: localDateSchema,
          countsBaseDay: z.boolean(),
          repeats: z.array(ddayRepeatSchema).refine(
            (repeats) =>
              new Set(repeats.map((repeat) => ("days" in repeat ? `days:${repeat.days}` : `months:${repeat.months}`))).size ===
                repeats.length,
            { message: "repeats must not contain the same interval twice." },
          ),
          endDate: localDateSchema.nullable(),
          overflowPolicy: z.enum(["clamp_to_month_end", "skip", "next_month_first_day"]),
        })
        .refine(
          (detail) => detail.endDate === null || detail.endDate >= detail.baseDate,
          { message: "endDate must not be before baseDate." },
        ),
    }),
  ),
});

// 태어난 연도와 알게 된 경로는 이 정보가 생기기 전 버전의 앱이 보내지 않으므로 선택 키로 받는다.
// 키가 없으면 DB 함수가 저장된 값을 유지하거나 기본값을 쓰고, 키가 있으면 null이어도 그 값으로 저장한다.
export const contactPushRequestSchema = z.object({
  contactList: z.array(
    z.object({
      id: z.string().uuid(),
      isFavorite: z.boolean(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
      detail: z
        .object({
          name: z.string(),
          description: z.string(),
          heightCentimeter: z.number().nullable(),
          footSizeMillimeter: z.number().int().nullable(),
          birthday: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).nullable(),
          birthdayCalendar: z.enum(["solar", "lunar"]).nullable(),
          birthYear: z.number().int().nullable().optional(),
          hometown: z.string(),
          howWeMet: z.string().optional(),
          phoneNumberList: z.array(
            z.object({
              number: z.string(),
            }),
          ),
        })
        .refine(
          (detail) => (detail.birthday === null) === (detail.birthdayCalendar === null),
          { message: "birthday and birthdayCalendar must be set together." },
        )
        .refine(
          (detail) =>
            detail.birthYear === undefined ||
            detail.birthday === null ||
            detail.birthYear === Number(detail.birthday.slice(0, 4)),
          { message: "birthYear must be the year of birthday." },
        ),
    }),
  ),
});

export const memoTagPushRequestSchema = z.object({
  memoTagList: z.array(
    z.object({
      memoId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const tagLinkPushRequestSchema = z.object({
  tagLinkList: z.array(
    z
      .object({
        fromTagId: z.string().uuid(),
        toTagId: z.string().uuid(),
        isDeleted: z.boolean(),
        updatedAt: z.string().datetime({ offset: true }),
        createdAt: z.string().datetime({ offset: true }),
      })
      // 태그 연결은 서로 다른 두 태그를 잇는 관계이므로 자기 자신을 가리킬 수 없다.
      .refine(
        (tagLink) => tagLink.fromTagId !== tagLink.toTagId,
        { message: "Tag link must point to another tag." },
      ),
  ),
});

export const webTagPushRequestSchema = z.object({
  webTagList: z.array(
    z.object({
      webId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const placeTagPushRequestSchema = z.object({
  placeTagList: z.array(
    z.object({
      placeId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const contactTagPushRequestSchema = z.object({
  contactTagList: z.array(
    z.object({
      contactId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const checklistTagPushRequestSchema = z.object({
  checklistTagList: z.array(
    z.object({
      checklistId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const musicTagPushRequestSchema = z.object({
  musicTagList: z.array(
    z.object({
      musicId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const qrTagPushRequestSchema = z.object({
  qrTagList: z.array(
    z.object({
      qrId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const ddayTagPushRequestSchema = z.object({
  ddayTagList: z.array(
    z.object({
      ddayId: z.string().uuid(),
      tagId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const memoPlacePushRequestSchema = z.object({
  memoPlaceList: z.array(
    z.object({
      memoId: z.string().uuid(),
      placeId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const memoWebPushRequestSchema = z.object({
  memoWebList: z.array(
    z.object({
      memoId: z.string().uuid(),
      webId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const memoContactPushRequestSchema = z.object({
  memoContactList: z.array(
    z.object({
      memoId: z.string().uuid(),
      contactId: z.string().uuid(),
      isDeleted: z.boolean(),
      updatedAt: z.string().datetime({ offset: true }),
      createdAt: z.string().datetime({ offset: true }),
    }),
  ),
});

export const pullRequestSchema = z.object({
  usn: z.number().int().nonnegative(),
});
