package com.sahayak.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactManager {
    private static final String PREF_NAME = "SahayakPrefs";
    private static final String KEY_CONTACTS = "emergency_contacts";
    private SharedPreferences sharedPreferences;

    public ContactManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addContact(String contact) {
        Set<String> contacts = sharedPreferences.getStringSet(KEY_CONTACTS, new HashSet<>());
        Set<String> newContacts = new HashSet<>(contacts);
        newContacts.add(contact);
        saveContacts(newContacts);
    }

    public List<String> getContacts() {
        Set<String> contacts = sharedPreferences.getStringSet(KEY_CONTACTS, new HashSet<>());
        return new ArrayList<>(contacts);
    }

    public void removeContact(String contact) {
        Set<String> contacts = sharedPreferences.getStringSet(KEY_CONTACTS, new HashSet<>());
        Set<String> newContacts = new HashSet<>(contacts);
        newContacts.remove(contact);
        saveContacts(newContacts);
    }

    public void updateContact(String oldContact, String newContact) {
        Set<String> contacts = sharedPreferences.getStringSet(KEY_CONTACTS, new HashSet<>());
        Set<String> newContacts = new HashSet<>(contacts);
        newContacts.remove(oldContact);
        newContacts.add(newContact);
        saveContacts(newContacts);
    }

    private void saveContacts(Set<String> contacts) {
        // SharedPreferences bug fix: hamesha remove karke naya set dalna chahiye
        sharedPreferences.edit().remove(KEY_CONTACTS).apply();
        sharedPreferences.edit().putStringSet(KEY_CONTACTS, contacts).apply();
    }
}
