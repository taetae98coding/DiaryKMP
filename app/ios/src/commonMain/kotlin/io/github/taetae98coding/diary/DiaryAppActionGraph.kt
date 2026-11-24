package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.AppActionGraph

// 운영체제가 앱을 화면 없이 시작해 앱 동작을 실행해도 그래프와 시작 작업이 준비된 뒤 처리하도록, 앱 동작은 항상 이 함수로 얻는다.
internal fun diaryAppActionGraph(): AppActionGraph = DiaryStartupInitializer.graph
