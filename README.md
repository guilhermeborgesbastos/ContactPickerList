# ContactPickerList
Lista de contato completa, com busca, marcadores com letras, ordem alfabética, envio de SMS e funcional para Android 6. Ideal para quem precisa listar e interagir com a agenda de contatos em seu App Android.

| Gif | Video |
| --- | --- |
| ![ContactPikerAnimated](https://meucomercioeletronico.com/tutorial/ContactPikerAnimated.gif)  | [![VIDEO](https://img.youtube.com/vi/r6qHrTARf2U/0.jpg)](https://www.youtube.com/watch?v=r6qHrTARf2U) |


## Instalação e uso
Basta importar o projeto do Git para o seu editor ( Android Studio / Eclipse, etc... )

## Classes
### MainActivity [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/java/guilherme/com/br/contactpikerlist/MainActivity.java)
Classe principal que gerencia o fragmento que possui a RecyclerView.
```
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
 * Guilherme Borges Bastos
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
```
Layout XML ( MainActivity ): [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/res/layout/activity_main.xml)
```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#fefefe"
    android:id="@+id/main"
    tools:context="guilherme.com.br.contactpikerlist.MainActivity">

    <FrameLayout
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>
```

### ContactUserFragment [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/java/guilherme/com/br/contactpikerlist/ContactUserFragment.java)
Classe que extende Fragment que possui todo o mecanismo de:
* Busca
* Ordenação
* Separador alfabético com letras
* Ordenação
* Botão com a Intent para envio de SMS
  
```
package guilherme.com.br.contactpikerlist;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import guilherme.com.br.contactpikerlist.pojos.Contatos;

/**
 * Created by guilh on 23/05/2016.
 */
public class ContactUserFragment extends Fragment {

    View rootView;
    public static MainActivity activity;

    RecyclerView list;
    public List<Contatos> recordSet;
    public ImageView ic_enable_search;
    LinearLayout ll;

    public ContactUserFragment(){
    }

    public ContactUserFragment(MainActivity _activity){
        activity = _activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        // Read and show the contacts
        applyContacts();
    }

    private void applyContacts() {
        // Verifica se o SDK é da versao que necessita de pedido de permissao OnTheFly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, activity.PERMISSIONS_REQUEST_READ_CONTACTS);
            //Depois deste ponto esperamos pelo Callback response em onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            showContacts();
        }
    }

    private void showContacts() {
    }

    // newInstance construtor para criar uma instancia com argumentos
    public static ContactUserFragment newInstance(MainActivity _activity) {
        ContactUserFragment fragmentFirst = new ContactUserFragment();
        activity = _activity;
        return fragmentFirst;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla o layout com fragmento
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.contact_user_fragment, container, false);
        }

        activity = (MainActivity) getActivity();

        init();

        return rootView;
    }

    private ContactListAdapter adapter;
    private SearchView search;
    private LoadContactsAyscn lca;
    private LinearLayout search_enable_block;
    private LinearLayout search_block;

    private void init() {

        //instancia e cria o listenner para a busca
        search = (SearchView) rootView.findViewById(R.id.search);

        search_enable_block = (LinearLayout) rootView.findViewById(R.id.search_enable_block);
        search_block = (LinearLayout) rootView.findViewById(R.id.search_block);

        ic_enable_search = (ImageView) rootView.findViewById(R.id.ic_enable_search);
        ic_enable_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_enable_block.setVisibility(View.GONE);
                search_block.setVisibility(View.VISIBLE);
            }
        });

        search.setOnQueryTextListener(listener);
        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                search_enable_block.setVisibility(View.VISIBLE);
                search_block.setVisibility(View.GONE);
                return true;
            }
        });

        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setFocusable(true);
            }
        });

        adapter = new ContactListAdapter(activity, "NoFriendListAdapter");
        ll = (LinearLayout) rootView.findViewById(R.id.LinearLayout1);
        list = (RecyclerView) rootView.findViewById(R.id.listView1);

        list.setHasFixedSize(true); // para performance

        LinearLayoutManager llm = new LinearLayoutManager(activity); // é o quem vai hospedar os ViewHolder's
        llm.setOrientation(LinearLayoutManager.VERTICAL); // orientaçao
        list.setLayoutManager(llm);

        lca = new LoadContactsAyscn();
        lca.execute();

    }


    class LoadContactsAyscn extends AsyncTask<Void, Void, ArrayList<String>> {
        ProgressDialog pd;

        public List<Contatos> recordSet;

        public List<Contatos> getRecordSet(){
            return recordSet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recordSet = new ArrayList<>();
            pd = ProgressDialog.show(activity, "Carregando contatos", "Por favor aguarde.");
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            ArrayList<String> contacts = new ArrayList<String>();

            Cursor c = activity.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    null, null, null);

            while (c.moveToNext()) {

                String contactName = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phNumber = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contacts.add(contactName + ":" + phNumber);

                Contatos contact =  new Contatos();
                contact.setNome(contactName);
                contact.setTelefone(phNumber);

                /*
                //código para gerar numero de telefone randomino
                // usado para nao export no exemplo a lista de usuário
                // que existem na minha agenda
                int min = 88000000;
                int max = 89999999;

                Random r = new Random();
                int randomNumeroTel = r.nextInt(max - min + 1) + min;

                contact.setTelefone("(11) 9 " + String.valueOf(randomNumeroTel));
                */


                contact.setSentInvite(false);
                recordSet.add(contact);


            }
            c.close();

            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<String> contacts) {
            super.onPostExecute(contacts);

            pd.cancel();

            recordSet = addAlphabets(sortList(recordSet));

            adapter.setData(recordSet);
            list.setAdapter(adapter);

        }

    }


    /* =============================================================
    Search
    ===============================================================*/

    public SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {

        public List<Contatos> recordSet;

        @Override
        public boolean onQueryTextChange(String query) {

            query = query.toLowerCase();

            if(recordSet == null){
                recordSet = lca.getRecordSet();
            }

            final List<Contatos> filteredList = new ArrayList<>();

            //varre a lista buscando registros que contenham a busca
            for (int i = 0; i < recordSet.size(); i++) {

                final String text = recordSet.get(i).getNome().toLowerCase();

                if (text.contains(query)) {
                    recordSet.get(i).setSentInvite(false);
                    filteredList.add(recordSet.get(i));
                }
            }

            adapter.setData(filteredList);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();  // data mudou
            return true;

        }


        public boolean onQueryTextSubmit(String query) {
            return false;
        }


    };

 /* =============================================================
    Order Alphabetic
    ===============================================================*/

    //faz a comparaçao entre os termos
    List<Contatos> sortList(List<Contatos> list) {
        Collections.sort(list, new Comparator<Contatos>() {
            @Override
            public int compare(Contatos teamMember1, Contatos teamMember2) {
                return teamMember1.getNome().compareTo(teamMember2.getNome());
            }
        });
        return list;
    }


    //organiza a lista em ordem alfabetica
    List<Contatos> addAlphabets(List<Contatos> list) {

        if(list.size() > 0) {

            int i = 0;
            List<Contatos> customList = new ArrayList<Contatos>();
            Contatos firstMember = new Contatos();
            firstMember.setNome(String.valueOf(list.get(0).getNome().charAt(0)));
            firstMember.setType(1);
            customList.add(firstMember);
            for (i = 0; i < list.size() - 1; i++) {
                Contatos teamMember = new Contatos();
                char name1 = list.get(i).getNome().charAt(0);
                char name2 = list.get(i + 1).getNome().charAt(0);
                if (name1 == name2) {
                    list.get(i).setType(2);
                    customList.add(list.get(i));
                } else {
                    list.get(i).setType(2);
                    customList.add(list.get(i));
                    teamMember.setNome(String.valueOf(name2));
                    teamMember.setType(1);
                    customList.add(teamMember);
                }
            }
            list.get(i).setType(2);
            customList.add(list.get(i));
            return customList;
        } else {
            Log.i("Busca", "Nada encontrado");

            Contatos contact =  new Contatos();
            contact.setNome("Nenhum contato encontrado com este nome");
            contact.setType(1);
            list.add(contact);
            return list;
        }
    }
}
```
Layout XML ( ContactUserFragment ): [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/res/layout/contact_user_fragment.xml)
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">


    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="65dp"
        android:orientation="horizontal"
        android:paddingTop="15dp"
        android:paddingBottom="15dp"
        android:background="@drawable/border_bottom"
        android:id="@+id/search_enable_block"
        android:layout_weight="0">

        <ImageView
            android:layout_width="35dp"
            android:layout_height="35dp"
            android:src="@drawable/agenda"
            android:layout_marginLeft="10dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:text="Agenda de Contatos"
            android:textSize="15sp"
            android:layout_weight="1"
            android:textColor="@color/greyDarkTxt"
            android:layout_marginLeft="15dp"
            android:paddingTop="8dp"
            />

        <ImageView
            android:layout_width="35dp"
            android:layout_height="35dp"
            android:padding="5dp"
            android:src="@drawable/ic_search"
            android:id="@+id/ic_enable_search"/>

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="70dp"
        android:orientation="horizontal"
        android:paddingTop="5dp"
        android:paddingBottom="5dp"
        android:background="#f2f2f2"
        android:id="@+id/search_block"
        android:visibility="gone">


        <SearchView
            android:id="@+id/search"
            android:layout_width="match_parent"
            android:layout_height="45dp"
            android:layout_gravity="center_vertical"
            android:textSize="16sp"
            android:layout_weight="5"
            android:layout_marginLeft="20dp"
            android:layout_marginRight="20dp"
            android:paddingLeft="10dp"
            android:paddingRight="10dp"
            android:hint="Buscar"
            style="@style/CustomSearchViewStyle"/>


    </LinearLayout>

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/LinearLayout1"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:layout_weight="1">


        <android.support.v7.widget.RecyclerView
            android:id="@+id/listView1"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:cacheColorHint="#ffccff"
            android:divider="#00ffff"
            android:dividerHeight="2sp"/>

    </LinearLayout>


</LinearLayout>
```

### ContactListAdapter [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/java/guilherme/com/br/contactpikerlist/ContactListAdapter.java)
Classe adaptador para a RecyclerView, ela recebe o @recordSet ( dados com Lista de contatos ) e faz a montagem das rows dentro da lista.

```
package guilherme.com.br.contactpikerlist;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import guilherme.com.br.contactpikerlist.pojos.Contatos;

/*
 * Created by guilh on 25/05/2016.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private static final int TYPE_LETTER = 1;
    private static final int TYPE_MEMBER = 2;
    public List<Contatos> recordSet;
    public MainActivity activity;
    public int type;

    public ContactListAdapter(Activity _activity, String _type){
        activity = (MainActivity) _activity;
    }

    public ContactListAdapter(List<Contatos> _recordSet, Activity _activity, int _type){
        recordSet = _recordSet;
        activity = (MainActivity) _activity;
        type = _type;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;

        if (recordSet.get(position).getType() == TYPE_LETTER) {
            viewType = TYPE_LETTER;
            type = TYPE_LETTER;
        } else if (recordSet.get(position).getType() == TYPE_MEMBER) {
            viewType = TYPE_MEMBER;
            type = TYPE_MEMBER;
        }

        return viewType;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {

            case TYPE_LETTER:
                ViewGroup vGroupLetter = (ViewGroup) mInflater.inflate(R.layout.contact_view_holder, viewGroup, false);
                ContactViewHolder letter = new ContactViewHolder(vGroupLetter, viewType);
                return letter;
            case TYPE_MEMBER:
                ViewGroup vGroupText = (ViewGroup) mInflater.inflate(R.layout.contact_view_holder, viewGroup, false);
                ContactViewHolder text = new ContactViewHolder(vGroupText, viewType);
                return text;
            default:
                ViewGroup vGroupText2 = (ViewGroup) mInflater.inflate(R.layout.contact_view_holder, viewGroup, false);
                ContactViewHolder text1 = new ContactViewHolder(vGroupText2, viewType);
                return text1;

        }

    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, final int position) {

        String letter = recordSet.get(position).getNome();

        if(type == TYPE_LETTER || letter.length() == 1){
            holder.letter.setText(recordSet.get(position).getNome());
        } else {

            Boolean sentInvate = recordSet.get(position).getSentInvite();

            if(!sentInvate){
                sentInvate = false;
            }

            holder.setData(position, sentInvate, recordSet.get(position).getTelefone(), recordSet.get(position).getNome());

            //quando ViewHolder está prestes a ficar visível
            holder.txtTitulo.setText(recordSet.get(position).getNome());
            holder.txtTexto1.setText(recordSet.get(position).getTelefone());
        }

    }


    @Override
    public int getItemCount() {
        //retona o número de itens da lista
        if (recordSet != null){
            return recordSet.size();
        }else{
            return 0;
        }
    }

    public void setData(List<Contatos> data) {
        this.recordSet = data;
    }


    /* =============================================================
    ViewHolder
    ===============================================================*/


    public class ContactViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitulo;
        public TextView txtTexto1;
        private int position;
        private String phoneNumber;
        private Boolean sentInvate;
        private Button btnInvite;
        private String nome;
        private TextView letter;
        private LinearLayout group_letter;
        private LinearLayout group;

        public  ContactViewHolder(View v, int viewType) {
            super(v);

            Log.i("ContactHolder ViewType", String.valueOf(viewType));

            group_letter = (LinearLayout) v.findViewById(R.id.group_letter);
            group = (LinearLayout) v.findViewById(R.id.group);

            if(viewType == TYPE_LETTER){

                letter = (TextView) v.findViewById(R.id.letter);

                group.setVisibility(View.GONE);
                group_letter.setVisibility(View.VISIBLE);

            } else {

                group.setVisibility(View.VISIBLE);
                group_letter.setVisibility(View.GONE);

                txtTitulo = (TextView) v.findViewById(R.id.txtTitulo);
                txtTexto1 = (TextView) v.findViewById(R.id.txtTexto1);
                btnInvite = (Button) v.findViewById(R.id.btnInvite);

                btnInvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i("btnInvite", "Send SMS: " + phoneNumber + " Nome: " + nome);

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
                        //TODO: mudar para email do suuário logado
                        String usrName = "guilhermebbastos";
                        String installUrlSite = "https://findme.com.br/add";
                        String message = "Me adicione no FindMe! Nome de usuário: " + usrName + " " + installUrlSite;
                        intent.putExtra("sms_body", message);
                        activity.startActivity(intent);

                    }
                });
            }
        }

        public void setData(int pos, boolean _sentInvate, String _phoneNumber, String _nome){
            position = pos;
            phoneNumber = _phoneNumber;
            sentInvate = _sentInvate;
            nome = _nome;

        }
    }



    /* =============================================================
    ViewHolder Letter
    ===============================================================*/

    public class ViewHolderLetter extends RecyclerView.ViewHolder {

        public TextView lebalLetter;
        public String letter;

        public ViewHolderLetter(View v) {
            super(v);
            lebalLetter =  (TextView) v.findViewById(R.id.letter);
        }

        public void setData(String _letter){
            letter = _letter;
        }
    }

}

```
Layout XML ( ContactListAdapter ) ViewHolder: [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/res/layout/contact_view_holder.xml)
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.v7.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:card_view="http://schemas.android.com/apk/res-auto"
    xmlns:fresco="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cv_item"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/white">

    <LinearLayout
        android:id="@+id/group"
        xmlns:my_app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="75dp"
        android:background="@drawable/bg_list_contact"
        android:gravity="center"
        android:paddingBottom="5dp"
        android:paddingLeft="10dp"
        android:paddingRight="10dp"
        android:paddingTop="5dp">

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_marginLeft="10dp"
            android:layout_marginRight="0dp"
            android:layout_weight="30"
            android:gravity="center_vertical"
            android:orientation="vertical">


            <TextView
                android:id="@+id/txtTitulo"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginRight="5dp"
                android:fontFamily="sans-serif-condensed"
                android:textColor="@color/black"
                android:text="Nome"
                android:textSize="22sp"
                android:textStyle="bold" />


            <TextView
                android:id="@+id/txtTexto1"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:fontFamily="sans-serif-condensed"
                android:textColor="#a5acb2"
                android:textSize="14sp"
                tools:text="Phone"/>

        </LinearLayout>

        <LinearLayout
            android:layout_width="1dp"
            android:layout_height="match_parent"
            android:layout_marginTop="5dp"
            android:layout_weight="10"
            android:gravity="center_vertical"
            android:orientation="vertical"
            android:layout_gravity="center"
            android:id="@+id/action_location">


            <Button
                android:layout_width="match_parent"
                android:layout_height="35dp"
                android:background="@drawable/invate_contact"
                android:textColor="@color/btnAdd"
                android:id="@+id/btnInvite"
                android:textSize="13sp"
                android:layout_gravity="center"
                android:layout_marginBottom="5dp"
                android:text="+ Adicionar"
                android:textAllCaps="false"
                android:paddingRight="5dp"
                android:paddingLeft="5dp"/>

        </LinearLayout>

    </LinearLayout>

    <LinearLayout
        android:id="@+id/group_letter"
        xmlns:my_app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:background="@drawable/bg_letter_list_contact"
        android:gravity="left"
        android:paddingBottom="5dp"
        android:paddingLeft="20dp"
        android:paddingRight="10dp"
        android:paddingTop="5dp"
        android:visibility="gone">

        <TextView
            android:id="@+id/letter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:fontFamily="sans-serif-condensed"
            android:textColor="#000000"
            android:textSize="18sp"
            tools:text="Letter"
            android:layout_marginTop="1dp"/>

    </LinearLayout>

</android.support.v7.widget.CardView>
```

### Contatos ( POJO ) [Visualizar Arquivo](https://github.com/guilhermeborgesbastos/ContactPickerList/blob/master/app/src/main/java/guilherme/com/br/contactpikerlist/pojos/Contatos.java)
Classe do tipo POJO para armazenar as infos que a lista de contatos do Android nos retornará.
```
package guilherme.com.br.contactpikerlist.pojos;

/**
 * Created by guilh on 25/05/2016.
 */
public class Contatos {

    private int id;
    private String nome;
    private String telefone;
    private String email;
    private Boolean sentInvite;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getSentInvite() {
        return sentInvite;
    }

    public void setSentInvite(Boolean sentInvite) {
        this.sentInvite = sentInvite;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
```

