package emilsoft.hackernews.connectivity;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class ConnectionSnackbar {

    public static void showConnectionLostSnackbar(View view) {
        Snackbar snackbar = Snackbar.make(view, "Connection Lost!", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}
