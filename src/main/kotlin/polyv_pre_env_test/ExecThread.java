package polyv_pre_env_test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static polyv_pre_env_test.PolyvPackageScript.closeIO;
import static polyv_pre_env_test.PolyvPackageScript.println;

/**
 * date: 2019/12/17
 * author: hwj
 * description:
 */
public class ExecThread extends Thread {
    InputStream is;
    boolean isEs;
    List<String>[] isMsgs;

    ExecThread(InputStream is, boolean isEs, List<String>... isMsgs) {
        this.is = is;
        this.isEs = isEs;
        this.isMsgs = isMsgs;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, "GBK"));
            String msg;
            //eg: INSTALL_APP(no devices) adb: error: failed to get feature set: no devices/emulators found isn'tEs wait
            while ((msg = br.readLine()) != null) {
                println(msg);
                if (isMsgs != null && isMsgs.length > 0) {
                    isMsgs[0].add(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(br);
        }
    }
}
