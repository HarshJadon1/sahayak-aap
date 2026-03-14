package com.sahayak.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private ContactManager contactManager;
    private EditText etContactNumber;
    private ListView lvContacts;
    private ArrayAdapter<String> adapter;
    private List<String> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactManager = new ContactManager(this);
        etContactNumber = findViewById(R.id.et_contact_number);
        lvContacts = findViewById(R.id.lv_contacts);

        loadContacts();

        findViewById(R.id.btn_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = etContactNumber.getText().toString().trim();
                if (!number.isEmpty()) {
                    contactManager.addContact(number);
                    etContactNumber.setText("");
                    loadContacts();
                    Toast.makeText(ContactsActivity.this, "Contact Added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadContacts() {
        contactsList = contactManager.getContacts();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        lvContacts.setAdapter(adapter);
    }
}
