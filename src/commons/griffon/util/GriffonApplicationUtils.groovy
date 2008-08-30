package griffon.util

class GriffonApplicationUtils {
   private GriffonApplicationUtils(){ }

   private static final boolean isWindows
   private static final boolean isWindows95
   private static final boolean isWindows98
   private static final boolean isWindowsNT
   private static final boolean isWindows2000
   private static final boolean isWindows2003
   private static final boolean isWindowsXP
   private static final boolean isWindowsVista

   private static final boolean isUnix
   private static final boolean isLinux
   private static final boolean isSolaris

   private static final boolean isMacOSX

   private static final String osArch
   private static final String osName
   private static final String osVersion
   private static final String javaVersion

   private static final boolean isJdk14
   private static final boolean isJdk15
   private static final boolean isJdk16

   static {
      osArch = System.getProperty("os.arch")
      osName = System.getProperty("os.name")
      
      switch( osName ) {
         case ~/Windows.*/:
            isWindows = true
            isWindows95 = osName =~ /95/ 
            isWindows98 = osName =~ /98/ 
            isWindowsNT = isWindows2000 = (osName =~ /XP/) || (osName =~ /NT/)
            isWindows2003 = osName =~ /2003/ 
            isWindowsXP = (osName =~ /XP/) || (osName =~ /2003/)
            isWindowsVista = osName =~ /Vista/ 
            break
         case ~/Linux.*/:
            isUnix = true
            isLinux = true
            break
         case ~/Solaris.*/:
         case ~/SunOS.*/:
            isUnix = true
            isSolaris = true
            break
         case ~/Mac OS.*/:
            isMacOSX = true
      }

      osVersion = System.getProperty("os.version")
      javaVersion = System.getProperty("java.version")
      switch( new BigDecimal(javaVersion[0..2]) ) {
         case {it >= 1.6} : isJdk16 = true
         case {it >= 1.5} : isJdk15 = true
         case {it >= 1.4} : isJdk14 = true
      }
   }
}
