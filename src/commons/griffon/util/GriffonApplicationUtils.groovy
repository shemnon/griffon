package griffon.util

class GriffonApplicationUtils {
   private GriffonApplicationUtils(){ }

   static final boolean isWindows
   static final boolean isWindows95
   static final boolean isWindows98
   static final boolean isWindowsNT
   static final boolean isWindows2000
   static final boolean isWindows2003
   static final boolean isWindowsXP
   static final boolean isWindowsVista

   static final boolean isUnix
   static final boolean isLinux
   static final boolean isSolaris

   static final boolean isMacOSX

   static final String osArch
   static final String osName
   static final String osVersion
   static final String javaVersion

   static final boolean isJdk14
   static final boolean isJdk15
   static final boolean isJdk16

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