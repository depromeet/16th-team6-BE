package com.deepromeet.atcha.support

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest

@ExtendWith(DatabaseCleanerExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "kakao.api.url=http://dummy",
        "jwt.access.secret=thisisfortestdGVzdEFjY2Vzc1NlY3JldEtleVZhbHVlMTIzNDU2Nzg=",
        "jwt.refresh.secret=thisisfortestddGVzdFJmZXNoU2VjcmV0S2V5VmFsdWUxMjM0NTY3OA"
    ]
)
abstract class BaseServiceTest
