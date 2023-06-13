-injars  build/libs/
-outjars build/proguard/

-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)

-dontwarn *
-dontwarn **
-dontoptimize

-keepnames class kotlin.coroutines.** { *; }
-keep class org.apache.** { *; }
-keep class org.tomlj.** { *; }
-keep class com.vdurmont.** { *; }
-keep class org.springframework.** { *; }
-keep class io.sentry.** { *; }
-keep enum * { public *; }

-keep public class kiinse.me.zonezero.plugin.ZoneZero { public *; }
-keep class kiinse.me.zonezero.plugin.commands.** { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.BlockPlaceListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.ChatListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.CommandsListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.DamageListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.ExitListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.FoodListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.InteractListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.JoinListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.InventoryListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.MiningListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.MoveListener { public *; }
-keep class kiinse.me.zonezero.plugin.listeners.QuitListener { public *; }

-repackageclasses 'kiinse.me.zonezero'
-keepattributes !LocalVariableTable,!LocalVariableTypeTable,Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable,*Annotation*,*Annotations*,EnclosingMethod