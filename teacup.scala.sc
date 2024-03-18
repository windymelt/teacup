//> using scala 3.4.0
//> using dep "com.lihaoyi::os-lib::0.9.3"

import os._

val cmds = args
os.proc(cmds)
  .call(
    cwd = pwd,
    stdout = Inherit,
    stderr = Inherit,
    propagateEnv = true
  )
