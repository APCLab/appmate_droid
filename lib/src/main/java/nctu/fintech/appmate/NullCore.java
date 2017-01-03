package nctu.fintech.appmate;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class NullCore extends TableCore {
    NullCore() {
        super();
    }

    @Override
    HttpURLConnection openUrl() throws IOException {
        throw new UnsupportedOperationException("illegal operation: open HttpURLConnection without indicate url.");
    }

    @Override
    HttpURLConnection openUrl(URL url) throws IOException {
        return openUrl();
    }

    @Override
    HttpURLConnection openUrl(int id) throws IOException {
        return openUrl();
    }

    @Override
    HttpURLConnection openUrl(String path) throws IOException {
        return openUrl();
    }

}
