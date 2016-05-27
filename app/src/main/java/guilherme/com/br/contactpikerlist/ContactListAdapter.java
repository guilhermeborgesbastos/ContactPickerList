package guilherme.com.br.contactpikerlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import guilherme.com.br.contactpikerlist.pojos.Contatos;

/*
 * Created by guilh on 02/03/2016.
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
