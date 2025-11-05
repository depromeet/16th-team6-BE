package com.deepromeet.atcha.app.api.request

data class AppVersionUpdateRequest(
    val version: String
) {
    init {
        require(version.matches(Regex("^v\\d+\\.\\d+\\.\\d+$"))) {
            "버전 형식이 올바르지 않습니다. 'vX.Y.Z' 형식이어야 합니다."
        }
    }
}
