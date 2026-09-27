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
          hometown: z.string(),
          phoneNumberList: z.array(
            z.object({
              number: z.string(),
            }),
          ),
        })
        .refine(
          (detail) => (detail.birthday === null) === (detail.birthdayCalendar === null),
          { message: "birthday and birthdayCalendar must be set together." },
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
