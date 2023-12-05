# execute with ~/Downloads/proguard-7.4.1/bin/proguard.sh @config.pro

-injars       /home/ben/IdeaProjects/vertx-dagger-app/iam-parent/iam-grpc/target/iam-grpc.jar
-outjars      output.jar
-libraryjars  /home/ben/.jdks/21/jmods/java.base.jmod(!**.jar;!module-info.class):/home/ben/IdeaProjects/vertx-dagger-app/iam-parent/iam-grpc/target/lib/
-dontwarn

-repackageclasses 'com.example.repackage'
-allowaccessmodification
-dontobfuscate
-keepattributes *Annotation*

-keep public class com.example.iam.** {
    public protected *;
}

-keep !public class com.example.iam.*Dagger* {
    public protected *;
}

-keep !public class com.example.iam.*_* {
    public protected *;
}

-keep public class io.netty.** {
    public protected *;
}

-keep public class io.vertx.** {
    public protected *;
}
