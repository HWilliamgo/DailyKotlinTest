package polyv_pre_env_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static polyv_pre_env_test.Command.ADB_DEVICES;
import static polyv_pre_env_test.Command.ASSEMBLE_DEBUG;
import static polyv_pre_env_test.Command.ASSEMBLE_RELEASE;
import static polyv_pre_env_test.Command.CHECKOUT_GITHUB_DEMO_BRANCH;
import static polyv_pre_env_test.Command.CHECK_ZIPALIGN;
import static polyv_pre_env_test.Command.CLEAN_GITHUB_DEMO;
import static polyv_pre_env_test.Command.CLONE_GITHUB_DEMO;
import static polyv_pre_env_test.Command.GEN_KEY;
import static polyv_pre_env_test.Command.INSTALL_APP;
import static polyv_pre_env_test.Command.LAUNCH_GITHUB_DEMO;
import static polyv_pre_env_test.Command.RERESETALL_GITHUB_DEMO;
import static polyv_pre_env_test.Command.SIGN_APK;
import static polyv_pre_env_test.Command.START_APP;
import static polyv_pre_env_test.Command.UNINSTALL_APP;
import static polyv_pre_env_test.Command.ZIPALIGN_APK;

import static polyv_pre_env_test.Mode.DEBUG;
import static polyv_pre_env_test.Mode.PRE_RELEASE;
import static polyv_pre_env_test.Mode.REGRESSION;
import static polyv_pre_env_test.Mode.RELEASE;


public class PolyvPackageScript {
    //config
    static final String MODULE_APPLICATION = "app";
    static final String MODULE_STORE_AAR = "commonui";
    static final String[] REMOTE_DEPENDS = {"com.easefun.polyv:polyvSDKCloudClass"};
    static final String[][] DEPEND_MODULES = {{"PolyvAndroidSDKBusiness", "business"}, {"PolyvAndroidSDKFoundation", "foundation"},
            {"PolyvAndroidCloudClassSDK", "polyvCloudClassSDK", "polyvLinkMic"}};
    static final String GITHUB_DEMO_URL = " http://github.com/polyv/polyv-android-cloudClass-sdk-demo.git";//use http
    static final String GITHUB_DEMO_BRANCH = "master";
    static final String GITHUB_DEMO_PARENT_DIRNAME = "github_demo";
    static final String GITHUB_DEMO_STORE_AAR_DIR = "libs";
    //variable
    static String DEMO_PATH;
    static String GITHUB_DEMO_PATH;
    static String GITHUB_DEMO_STORE_AAR_PATH;
    static List<String> THIRDLIB_DEPENDS;

    public static void main(String[] args) throws Exception {
        long packageStartTime = System.currentTimeMillis();

        Map<String, String> map = new HashMap<>();
        if (args != null && args.length > 0) {
            if (args.length % 2 != 0) {
                throw new Exception("params num incorrect");
            }
            for (int i = 0; i < args.length - 1; i += 2) {
                map.put(args[i], args[i + 1]);
            }
        }
        //param
        Param.setMode(map.get(Param.PARAM_MODE));
        Param.setClearUser(map.get(Param.PARAM_CLEAR_USER));

        DEMO_PATH = new File("").getCanonicalPath();

        boolean result = false;
        switch (Param.MODE) {
            case DEBUG:
                result = debugPackage();
                break;
            case REGRESSION:
                result = regressionPackage();
                break;
            case PRE_RELEASE:
                result = preReleasePackage();
                break;
            case RELEASE:
                result = preReleasePackage();
                break;
        }
        println(result ? Param.MODE.name() + " 打包成功" + genCostTimeSecMsg(packageStartTime)
                : Param.MODE.name() + " 打包失败" + genCostTimeSecMsg(packageStartTime));
    }

    static boolean preReleasePackage() {
        if (initGithubProject()
                && syncGithubDemo()
                && copyAarToGithubDemoModule()
                && copyThirdLibDependsToGithubDemoModule()
                && replaceRemoteDependsWithLocalAar()
                && assembleRelease(GITHUB_DEMO_PATH)
                && installApp(GITHUB_DEMO_PATH)) {
            return true;
        }
        return false;
    }

    static boolean regressionPackage() {
        if (assembleRelease(DEMO_PATH)
                && installApp(DEMO_PATH)) {
            return true;
        }
        return false;
    }

    static boolean debugPackage() {
        if (assembleDebug(DEMO_PATH)
                && installApp(DEMO_PATH)) {
            return true;
        }
        return false;
    }


    static boolean genKeySignApk(String projectPath) {
        String keyName = "polyv";
        String ou = keyName;
        String storepass = "123456";
        String alias = "key";
        String keypass = "123456";
        String keystore = projectPath + File.separator + keyName + ".jks";//existed cloud error
        String apkFilePath = getApkFilePath(projectPath);
        String signApkFilePath = apkFilePath.substring(0, apkFilePath.lastIndexOf(".")) + "-sign" + apkFilePath.substring(apkFilePath.lastIndexOf("."));
        return waitExec(String.format(GEN_KEY, alias, keypass, keystore, storepass, ou))
                && waitExec(String.format(SIGN_APK, storepass, keystore, apkFilePath, signApkFilePath));
    }


    static boolean syncGithubDemo() {
        return waitExec(String.format(LAUNCH_GITHUB_DEMO, GITHUB_DEMO_PATH, Param.CLEAR_USER));
    }

    static boolean assembleRelease(String projectPath) {
        return deleteOldApk(projectPath) && waitExec(String.format(ASSEMBLE_RELEASE, projectPath, projectPath));
    }

    static boolean assembleDebug(String projectPath) {
        return deleteOldApk(projectPath) && waitExec(String.format(ASSEMBLE_DEBUG, projectPath, projectPath));
    }

    static boolean deleteOldApk(String projectPath) {
        String apkFilePath = getApkFilePath(projectPath);
        if (new File(apkFilePath).isFile()) {
            return FileUtils.deleteAllInDir(new File(apkFilePath).getParentFile());
        } else {
            return FileUtils.deleteAllInDir(apkFilePath);
        }
    }

    static boolean replaceRemoteDependsWithLocalAar() {
        long methodStartTime = System.currentTimeMillis();
        String storeAarModuleBuildGradleFilePath = GITHUB_DEMO_PATH + File.separator + MODULE_STORE_AAR + File.separator + "build.gradle";
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(storeAarModuleBuildGradleFilePath));
            bw = new BufferedWriter(new FileWriter(storeAarModuleBuildGradleFilePath + ".temp"));
            String msg;
            boolean hasWriteLocalAar = false;
            while ((msg = br.readLine()) != null) {
                boolean msgHasRemoteDepend = false;
                for (String s : REMOTE_DEPENDS) {
                    if (msg.contains(s) && !msg.contains("//")) {
                        msgHasRemoteDepend = true;
                        if (!hasWriteLocalAar) {
                            hasWriteLocalAar = true;
                            File[] releaseAarPaths = new File(GITHUB_DEMO_STORE_AAR_PATH).listFiles();
                            for (File releaseAarPath : releaseAarPaths) {
                                if (releaseAarPath.getName().contains("-release")) {
                                    bw.write(msg.substring(0, msg.indexOf(s) - 1) + "(name: '" +
                                            releaseAarPath.getName().substring(0, releaseAarPath.getName().lastIndexOf(".")) +
                                            "', ext: '" + releaseAarPath.getName().substring(releaseAarPath.getName().lastIndexOf(".") + 1) + "')");
                                    bw.newLine();
                                    bw.flush();
                                }
                            }
                        }
                        break;
                    }
                }
                if (!msgHasRemoteDepend) {
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                }
            }
            closeIO(br);
            closeIO(bw);
            if (!new File(storeAarModuleBuildGradleFilePath).exists() || new File(storeAarModuleBuildGradleFilePath).delete()) {
                if (!new File(storeAarModuleBuildGradleFilePath + ".temp").renameTo(new File(storeAarModuleBuildGradleFilePath))) {
                    println("renameTo storeAarModuleBuildGradleFilePath error");
                    return false;
                }
            } else {
                println("delete storeAarModuleBuildGradleFilePath error");
                return false;
            }
            String githubDemoBuildGradlePath = GITHUB_DEMO_PATH + File.separator + "build.gradle";
            bw = new BufferedWriter(new FileWriter(githubDemoBuildGradlePath, true));
            bw.newLine();
            List<String> writeMsg = new ArrayList<>();
            writeMsg.add("allprojects {");
            writeMsg.add("    repositories {");
            writeMsg.add("        flatDir {");
            writeMsg.add("            dirs \"$rootProject.projectDir/" + MODULE_STORE_AAR + "/" + GITHUB_DEMO_STORE_AAR_DIR + "\"");
            writeMsg.add("        }");
            writeMsg.add("    }");
            writeMsg.add("}");
            for (String s : writeMsg) {
                bw.write(s);
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            println("replaceRemoteDependsWithLocalAar error");
            return false;
        } finally {
            closeIO(br);
            closeIO(bw);
        }
        println("replaceRemoteDependsWithLocalAar success" + genCostTimeSecMsg(methodStartTime));
        return true;
    }

    static boolean getModuleThirdLibDepends() {
        THIRDLIB_DEPENDS = new ArrayList<>();
        long methodStartTime = System.currentTimeMillis();
        for (int i = 0; i < DEPEND_MODULES.length; i++) {
            for (int j = 1; j < DEPEND_MODULES[i].length; j++) {
                String moduleBuildGradleFilePath = new File(DEMO_PATH).getParent() + File.separator + DEPEND_MODULES[i][0] + File.separator +
                        DEPEND_MODULES[i][j] + File.separator + "build.gradle";
                THIRDLIB_DEPENDS.add("//" + DEPEND_MODULES[i][j] + " dependencies");
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(moduleBuildGradleFilePath));
                    String msg;
                    int condition = 0, count = 0;
                    while ((msg = br.readLine()) != null) {
                        if (msg.startsWith("dependencies {") || msg.startsWith("dependencies{")) {
                            condition = 1;
                            count = 0;
                        }
                        if (condition != 0) {
                            if ((!msg.contains("Implementation") && !msg.contains("Api") && !msg.contains("Compile") && !msg.contains("Processor")
                                    && !msg.contains("fileTree(") && !msg.contains("project(") && !msg.contains("(name"))
                                    || msg.contains("//")) {
                                THIRDLIB_DEPENDS.add(msg);
                            }
                            if (msg.contains("}") && !msg.contains("//")) {
                                condition = condition == 2 ? 1 : 0;
                            }
                            if (msg.contains("{") && !msg.contains("//") && ++count >= 2) {
                                condition = 2;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    println("getModuleThirdLibDepends error");
                    return false;
                } finally {
                    closeIO(br);
                }
            }
        }
        println("getModuleThirdLibDepends success" + genCostTimeSecMsg(methodStartTime));
        return true;
    }

    static boolean copyThirdLibDependsToGithubDemoModule() {
        if (getModuleThirdLibDepends()) {
            long methodStartTime = System.currentTimeMillis();
            String storeAarModuleBuildGradleFilePath = GITHUB_DEMO_PATH + File.separator + MODULE_STORE_AAR + File.separator + "build.gradle";
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                br = new BufferedReader(new FileReader(storeAarModuleBuildGradleFilePath));
                bw = new BufferedWriter(new FileWriter(storeAarModuleBuildGradleFilePath + ".temp"));
                String msg;
                int condition = 0, count = 0;
                while ((msg = br.readLine()) != null) {
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                    if (msg.startsWith("dependencies {") || msg.startsWith("dependencies{")) {
                        condition = 1;
                        count = 0;
                    }
                    if (condition != 0) {
                        if (msg.contains("}") && !msg.contains("//")) {
                            condition = condition == 2 ? 1 : 0;
                            if (condition == 0 && THIRDLIB_DEPENDS.size() > 0) {
                                for (String s : THIRDLIB_DEPENDS) {
                                    bw.write(s);
                                    bw.newLine();
                                    bw.flush();
                                }
                                THIRDLIB_DEPENDS.clear();
                            }
                        }
                        if (msg.contains("{") && !msg.contains("//") && ++count >= 2) {
                            condition = 2;
                        }
                    }

                }
                closeIO(br);
                closeIO(bw);
                if (!new File(storeAarModuleBuildGradleFilePath).exists() || new File(storeAarModuleBuildGradleFilePath).delete()) {
                    if (!new File(storeAarModuleBuildGradleFilePath + ".temp").renameTo(new File(storeAarModuleBuildGradleFilePath))) {
                        println("renameTo storeAarModuleBuildGradleFilePath error");
                        return false;
                    }
                } else {
                    println("delete storeAarModuleBuildGradleFilePath error");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                println("copyThirdLibDependsToGithubDemoModule error");
                return false;
            } finally {
                closeIO(br);
                closeIO(bw);
            }
            println("copyThirdLibDependsToGithubDemoModule success" + genCostTimeSecMsg(methodStartTime));
            return true;
        }
        return false;
    }

    static boolean copyAarToGithubDemoModule() {
        long methodStartTime = System.currentTimeMillis();
        GITHUB_DEMO_STORE_AAR_PATH = GITHUB_DEMO_PATH + File.separator + MODULE_STORE_AAR + File.separator + GITHUB_DEMO_STORE_AAR_DIR;
        if (!new File(GITHUB_DEMO_STORE_AAR_PATH).exists()) {
            if (!new File(GITHUB_DEMO_STORE_AAR_PATH).mkdirs()) {
                println("make storeArrPath error");
                return false;
            }
        }
        if (new File(GITHUB_DEMO_STORE_AAR_PATH).exists()) {
            for (int i = 0; i < DEPEND_MODULES.length; i++) {
                for (int j = 1; j < DEPEND_MODULES[i].length; j++) {
                    String fromAarPath = new File(DEMO_PATH).getParent() + File.separator + DEPEND_MODULES[i][0] + File.separator +
                            DEPEND_MODULES[i][j] + File.separator + "build" + File.separator + "outputs" + File.separator + "aar";
                    File[] aarFiles = new File(fromAarPath).listFiles();
                    boolean hasReleaseAar = false;
                    for (File aarFile : aarFiles) {
                        if (aarFile.getName().contains("-release")) {
                            hasReleaseAar = true;
                            if (!FileUtils.copyFile(aarFile.getAbsolutePath(), GITHUB_DEMO_STORE_AAR_PATH + File.separator + aarFile.getName(), new FileUtils.OnReplaceListener() {
                                @Override
                                public boolean onReplace() {
                                    return true;
                                }
                            })) {
                                println(DEPEND_MODULES[i] + " copy aar error");
                                return false;
                            }
                            break;
                        }
                    }
                    if (!hasReleaseAar) {
                        println(DEPEND_MODULES[i] + " module is not has release aar");
                        return false;
                    }
                }
            }
            println("copyAarToGithubDemoModule success" + genCostTimeSecMsg(methodStartTime));
            return true;
        }
        return false;
    }

    static boolean initGithubProject() {
        String githubDemoParentPath = DEMO_PATH + File.separator + GITHUB_DEMO_PARENT_DIRNAME;
        GITHUB_DEMO_PATH = githubDemoParentPath + File.separator
                + GITHUB_DEMO_URL.substring(GITHUB_DEMO_URL.lastIndexOf("/") + 1, GITHUB_DEMO_URL.lastIndexOf("."));
        if (!new File(GITHUB_DEMO_PATH).exists()) {
            if (!new File(githubDemoParentPath).exists()) {
                if (!new File(githubDemoParentPath).mkdirs()) {
                    println("make githubDemoParentPath error");
                    return false;
                }
            }
            if (!waitExec(String.format(CLONE_GITHUB_DEMO, githubDemoParentPath))) {
                return false;
            }
        }
        if (new File(GITHUB_DEMO_PATH).exists()) {
            int childFileCount = new File(GITHUB_DEMO_PATH).list().length;
            //.git app gradle .gitignore build.gradle gradle.properties gradlew gradlew.bat settings.gradle
            if (childFileCount < 9) {
                if (!FileUtils.deleteAllInDir(GITHUB_DEMO_PATH) || !new File(GITHUB_DEMO_PATH).delete()) {
                    println("clean github_demo_path error");
                    return false;
                }
                if (!waitExec(String.format(CLONE_GITHUB_DEMO, githubDemoParentPath))) {
                    return false;
                }
            }
            if (waitExec(String.format(RERESETALL_GITHUB_DEMO, GITHUB_DEMO_PATH))
                    && waitExec(String.format(CLEAN_GITHUB_DEMO, GITHUB_DEMO_PATH))
                    && waitExec(String.format(CHECKOUT_GITHUB_DEMO_BRANCH, GITHUB_DEMO_PATH))
                /*&& waitExec(String.format(PULL_GITHUB_DEMO, GITHUB_DEMO_PATH))*/) {
                return true;
            }
        }
        return false;
    }

    static boolean installApp(String projectPath) {
        if (!hasDeviceAttached())
            return true;
        String apkFilePath = getApkFilePath(projectPath);
        if (apkFilePath.endsWith("-unsigned.apk")) {
            if (!genKeySignApk(projectPath)) {
                return false;
            } else {
                apkFilePath = getApkFilePath(projectPath);
            }
        }
        List<String> isMsgs = new ArrayList<>();
        boolean result = waitExec(String.format(INSTALL_APP, apkFilePath), isMsgs);
        if (!result) {
            for (String s : isMsgs) {
                //eg: Failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES: Package /data/app/vmdl793226418.tmp/base.apk has no certificates at entry AndroidManifest.xml]
                if (s.contains("INSTALL_PARSE_FAILED_NO_CERTIFICATES")) {
                    if (!genKeySignApk(projectPath)) {
                        return false;
                    } else {
                        apkFilePath = getApkFilePath(projectPath);
                        return waitExec(String.format(INSTALL_APP, apkFilePath))
                                && waitExec(String.format(START_APP, getPackageName(projectPath)));
                    }
                }
            }
            result = waitExec(String.format(UNINSTALL_APP, getPackageName(projectPath)))
                    && waitExec(String.format(INSTALL_APP, apkFilePath));
        }
        return result && waitExec(String.format(START_APP, getPackageName(projectPath)));
    }

    static boolean hasDeviceAttached() {
        List<String> isMsgs = new ArrayList<>();
        waitExec(ADB_DEVICES, isMsgs);
        for (String msg : isMsgs) {
            if (msg.endsWith("device")) {
                return true;
            }
        }
        return false;
    }

    static boolean waitExec(String command, List<String>... isMsgs) {
        return waitExec(new String[]{command}, isMsgs);
    }

    static boolean waitExec(String command[], List<String>... isMsgs) {
        long commandStartTime = System.currentTimeMillis();
        println(System.getProperty("line.separator") + "exec command: " + Arrays.toString(command));
        Process p = null;
        try {
            p = command.length == 1 ? Runtime.getRuntime().exec(command[0]) : Runtime.getRuntime().exec(command);
            ExecThread isThread = new ExecThread(p.getInputStream(), false, isMsgs);
            ExecThread esThread = new ExecThread(p.getErrorStream(), true, isMsgs);
            isThread.start();
            esThread.start();
            p.waitFor();
            isThread.join();
            esThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        println("command end" + genCostTimeSecMsg(commandStartTime));
        return (p != null ? p.exitValue() : 1) == 0;
    }

    static String getPackageName(String projectPath) {
        String appBuildGradlePath = projectPath + File.separator + MODULE_APPLICATION + File.separator + "build.gradle";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(appBuildGradlePath));
            String msg;
            while ((msg = br.readLine()) != null) {
                if (msg.contains("applicationId")) {
                    int index = msg.indexOf("applicationId");
                    return msg.substring(msg.indexOf("\"", index) + 1, msg.indexOf("\"", msg.indexOf("\"", index) + 1));
                }
            }
            closeIO(br);
            String manifestPath = projectPath + File.separator + MODULE_APPLICATION + File.separator + "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";
            br = new BufferedReader(new FileReader(manifestPath));
            while ((msg = br.readLine()) != null) {
                if (msg.contains("package")) {
                    int index = msg.indexOf("package");
                    return msg.substring(msg.indexOf("\"", index) + 1, msg.indexOf("\"", msg.indexOf("\"", index) + 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(br);
        }
        return null;
    }

    static String getApkFilePath(String projectPath) {
        String apkFilePath = projectPath + File.separator + MODULE_APPLICATION + File.separator + "build" + File.separator + "outputs" + File.separator + "apk";
        if (Param.MODE == Mode.DEBUG) {
            apkFilePath += File.separator + "debug";
        } else if (Param.MODE == Mode.REGRESSION
                || Param.MODE == Mode.PRE_RELEASE
                || Param.MODE == Mode.RELEASE) {
            apkFilePath += File.separator + "release";
        }
        File[] apkFilePaths = new File(apkFilePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        });
        if (apkFilePaths != null && apkFilePaths.length > 0) {
            Arrays.sort(apkFilePaths, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    long value = f1.lastModified() - f2.lastModified();
                    return value > 0 ? -1 : value == 0 ? 0 : 1;
                }
            });
            apkFilePath = apkFilePaths[0].getAbsolutePath();
        }
        return apkFilePath;
    }

    static void closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String genCostTimeSecMsg(long startTime) {
        return "(" + (System.currentTimeMillis() - startTime) / 1000.0f + "s)";
    }

    static void println(String msg) {
        System.out.println(msg);
    }
}

