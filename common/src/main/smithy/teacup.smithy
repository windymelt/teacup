$version: "2"

namespace teacup.api

use alloy#simpleRestJson

@simpleRestJson
service Teacup {
    operations: [RunDaemon, ListDaemon]
}

@http(method: "POST", uri: "/daemons")
operation RunDaemon {
    input := {
        @required
        commands: Commands
    }
    output := {
        @required
        success: Boolean
        message: String
    }
}

@readonly
@http(method: "GET", uri: "/daemons")
operation ListDaemon {
    output := {
        @required
        id: DaemonId
    }
}

list Commands {
    member: String
}

integer DaemonId
