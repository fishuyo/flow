lazy val getLibs:TaskKey[Boolean] = TaskKey[Boolean]("Download unmanaged libs.")

getLibs := {
  val dir = baseDirectory.value / "lib"
  UnmanagedLibs.getHid(dir)
  UnmanagedLibs.getGlulogic(dir)
  UnmanagedLibs.getOpenNI2(dir)
  true
}
compile in Compile <<= (compile in Compile).dependsOn(getLibs)
