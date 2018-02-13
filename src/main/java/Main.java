import com.mmz.specs.application.core.server.Server;
import com.mmz.specs.application.utils.CoreUtils;
import com.mmz.specs.application.utils.Logging;

public class Main {

    public static void main(final String[] args) {
        new Logging();
        CoreUtils.manageArguments(args);
    }
}