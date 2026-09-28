package com.sahayak.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private ContactManager contactManager;
    private ListView lvContacts;
    private MaterialButton btnOpenAddScreen;
    private List<String> contactsList;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactManager = new ContactManager(this);
        lvContacts = findViewById(R.id.lv_contacts);
        btnOpenAddScreen = findViewById(R.id.btn_open_add_screen);

        loadContacts();

        btnOpenAddScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the new OTP-based Add Contact screen
                Intent intent = new Intent(ContactsActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from AddContactActivity
        loadContacts();
    }

    private void loadContacts() {
        contactsList = contactManager.getContacts();
        adapter = new ContactAdapter();
        lvContacts.setAdapter(adapter);
    }

    private class ContactAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactsList.size();
        }

        @Override
        public Object getItem(int position) {
            return contactsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ContactsActivity.this).inflate(R.layout.item_contact, parent, false);
            }

            String contact = contactsList.get(position);
            TextView tvNumber = convertView.findViewById(R.id.tv_contact_number);
            ImageButton btnEdit = convertView.findViewById(R.id.btn_edit);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_delete);

            String displayName = contact;
            if (contact != null && contact.contains(":")) {
                String[] parts = contact.split(":", 2);
                displayName = parts[0] + " (" + parts[1] + ")";
            }
            tvNumber.setText(displayName);

            // Edit logic updated to also use the OTP screen for verification
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactsActivity.this, AddContactActivity.class);
                    intent.putExtra("edit_number", contact);
                    startActivity(intent);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactManager.removeContact(contact);
                    loadContacts();
                    Toast.makeText(ContactsActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }
}
