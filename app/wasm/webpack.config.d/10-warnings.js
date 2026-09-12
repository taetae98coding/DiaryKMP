// Ktor가 wasm 대상에서 import(name)으로 모듈을 동적 로드해 webpack이 정적 분석을 포기한다.
// Kotlin/Wasm이 생성하는 import-object에 그대로 들어가므로 이 발생지에만 한정해 무시한다.
config.ignoreWarnings = (config.ignoreWarnings || []).concat([
    {
        module: /diary\.import-object\.mjs$/,
        message: /Critical dependency: the request of a dependency is an expression/,
    },
]);

// Compose Multiplatform wasm 번들은 skiko를 포함해 webpack 기본 권장 크기(244 KiB)를 항상 넘는다.
config.performance = Object.assign({}, config.performance, { hints: false });
