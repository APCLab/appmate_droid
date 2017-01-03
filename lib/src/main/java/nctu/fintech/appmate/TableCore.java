package nctu.fintech.appmate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class TableCore extends DbCore {

    final URL url;
    final String tableName;

    TableCore(String host, String tableName) {
        this(new DbCore(host), tableName);
    }

    TableCore(String host, String username, String password, String tableName) {
        this(new DbCore(host, username, password), tableName);
    }

    TableCore(DbCore core, String tableName) {
        super(core);
        this.tableName = tableName;
        this.url = generateTableUrl(super.url, tableName);
    }

    TableCore() {
        super();
        tableName = null;
        url = null;
    }

    /**
     * Get table api URL.
     *
     * @param apiRoot a url referrer to api root
     * @param table   table name
     * @return a url referrer to table root
     */
    private static URL generateTableUrl(URL apiRoot, String table) {
        try {
            return new URL(apiRoot, "./" + table + "/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal table name", e);
        }
    }

    @Override
    HttpURLConnection openUrl() throws IOException {
        return openUrl(url);
    }

    HttpURLConnection openUrl(String path) throws IOException {
        URL url = new URL(this.url, path);
        return openUrl(url);
    }

    HttpURLConnection openUrl(int id) throws IOException {
        return openUrl("./" + id + "/");
    }

}
