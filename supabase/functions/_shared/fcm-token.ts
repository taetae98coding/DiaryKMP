import { z } from "npm:zod@3";

// 시간대와 언어가 함께 오면 등록, 둘이 비어 있으면 해제다. 게스트는 세션이 없어 해제 요청에 인증을 붙일 수 없다.
export const fcmTokenSubmitRequestSchema = z.union([
  z.object({
    token: z.string().min(1),
    timeZone: z.string().min(1),
    language: z.string().min(1),
  }),
  z.object({
    token: z.string().min(1),
    timeZone: z.null().optional(),
    language: z.null().optional(),
  }),
]);
