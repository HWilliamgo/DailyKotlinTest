package polyv_pre_env_test;

import java.io.File;

import static polyv_pre_env_test.PolyvPackageScript.GITHUB_DEMO_BRANCH;
import static polyv_pre_env_test.PolyvPackageScript.GITHUB_DEMO_URL;

/**
 * date: 2019/12/17
 * author: hwj
 * description:
 */
public interface Command {
    String GRADLEW = System.getProperties().getProperty("os.name").toLowerCase().contains("windows")/*eg: Windows 10*/ ? "gradlew.bat" : "gradlew";
    String APKSIGNER = System.getProperties().getProperty("os.name").toLowerCase().contains("windows")/*eg: Windows 10*/ ? "apksigner.bat" : "apksigner";
    String ASSEMBLE_DEBUG = "%s" + File.separator + GRADLEW + " assembleDebug -p %s";//-p projectPath
    String ASSEMBLE_RELEASE = "%s" + File.separator + GRADLEW + " assembleRelease -p %s";
    String ADB_DEVICES = "adb devices";
    String UNINSTALL_APP = "adb uninstall %s";
    String INSTALL_APP = "adb install %s";//(cover l h -r -d invalid)
    String START_APP = "adb shell monkey -p %s -c android.intent.category.LAUNCHER 1";
    String CLONE_GITHUB_DEMO = "git -C \"%s\" clone " + GITHUB_DEMO_URL;
    //add delete nsp
    String CHECKOUT_GITHUB_DEMO = "git -C \"%s\" checkout .";//local
    String CLEAN_GITHUB_DEMO = "git -C \"%s\" clean -df";
    //add nsp, not reset .gitignore about file
    String RERESETALL_GITHUB_DEMO = "git -C \"%s\" reset --hard";//local, HEAD remote, reset HEAD --hard
    String CHECKOUT_GITHUB_DEMO_BRANCH = "git -C \"%s\" checkout " + GITHUB_DEMO_BRANCH;
    String PULL_GITHUB_DEMO = "git -C \"%s\" pull";
    String LAUNCH_GITHUB_DEMO = GRADLEW + " launch -PdemoPath=%s -PclearUser=%s";
    String CHECK_ZIPALIGN = "zipalign -c -v 4 %s";
    String ZIPALIGN_APK = "zipalign -f -v 4 %s %s";
    String GEN_KEY = "keytool -genkey -alias %s -keypass %s -keyalg RSA -validity 36500 -keystore %s -storepass %s -dname ou=%s";
    String SIGN_APK = APKSIGNER + " sign --ks-pass pass:%s --ks %s --in %s --out %s";//storepass(and need keypass same)
}
