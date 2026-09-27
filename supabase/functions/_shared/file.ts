export const FILE_BUCKET = "file";

// 버킷의 file_size_limit, file 표의 size 제약과 같은 값이다.
export const MAX_FILE_BYTES = 50 * 1024 * 1024;

export const MAX_FILE_NAME_LENGTH = 255;

// 제목과 설명을 함께 보내는 요청은 파일 크기만 따로 알 수 없어, 요청 전체 크기를 거를 때 이만큼 더 받는다.
export const MAX_FILE_TEXT_BYTES = 1024 * 1024;
