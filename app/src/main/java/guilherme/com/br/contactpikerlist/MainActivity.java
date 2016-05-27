package guilherme.com.br.contactpikerlist;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * MainActivity
 * Guilherme Borges bastos
 * guilhermeborgesbastos@gmail.com
 * 05/27/2016
 * Fb: https://www.facebook.com/AndroidNaPratica/
 */
public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private Fragment fragment;
    private Toast mToast;
    private ContactUserFragment contactUserFragment;

    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactUserFragment = new ContactUserFragment(this);
        root = (FrameLayout) findViewById(R.id.main);

        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentByTag("ContactUserFragment");

        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.content, contactUserFragment, "ContactUserFragment");
            ft.commit();
        }

    }


}

