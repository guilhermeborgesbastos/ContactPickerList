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
import android.widget.Button;
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
