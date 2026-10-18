-- v1·v2 Edge Function이 버전을 붙인 함수로 옮겨 가 더 이상 부르는 곳이 없다.
drop function if exists public.push_contacts(jsonb);
drop function if exists public.pull_contacts(bigint);
