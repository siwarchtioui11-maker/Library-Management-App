/*package com.example.publiclibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "LDB";
    private static final int DB_VERSION = 1;

    private static final String USER_TABLE = "user";
    private static final String BOOK_TABLE = "book";
    private static final String TABLE_MEMBER = "members";
    private static final String RESERVE_TABLE = "book_reserve";

    // User table columns
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirm_password";
    private static final String ROLE = "role"; // admin ou etudiant

    // Book table columns
    private static final String BOOK_ID = "bid";
    private static final String BOOK_NAME = "bname";
    private static final String BOOK_AUTHOR = "bauthor";
    private static final String BOOK_PUBLISHER = "bpublisher";
    private static final String BOOK_QUANTITY = "bquantity";
    private static final String BOOK_AVAILABLE = "bavailable";

    // Member table columns
    private static final String MEMBER_ID = "id";
    private static final String MEMBER_NAME = "name";
    private static final String MEMBER_EMAIL = "email";
    private static final String MEMBER_PHONE = "phone";
    private static final String MEMBER_ADDRESS = "address";

    // Reserve table columns
    private static final String RESERVE_ID = "reserve_id";
    private static final String RESERVE_BOOK_ID = "book_id";
    private static final String RESERVE_MEMBER_NAME = "member_name";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME + " TEXT, "
                + EMAIL + " TEXT, "
                + PASSWORD + " TEXT, "
                + CONFIRM_PASSWORD + " TEXT, "
                + ROLE + " TEXT)");

        // Book table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BOOK_TABLE + "("
                + BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BOOK_NAME + " TEXT, "
                + BOOK_AUTHOR + " TEXT, "
                + BOOK_PUBLISHER + " TEXT, "
                + BOOK_QUANTITY + " INTEGER, "
                + BOOK_AVAILABLE + " BOOL)");

        // Member table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MEMBER + "("
                + MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MEMBER_NAME + " TEXT, "
                + MEMBER_EMAIL + " TEXT, "
                + MEMBER_PHONE + " TEXT, "
                + MEMBER_ADDRESS + " TEXT)");

        // Reserve table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RESERVE_TABLE + "("
                + RESERVE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RESERVE_BOOK_ID + " INTEGER, "
                + RESERVE_MEMBER_NAME + " TEXT, "
                + "FOREIGN KEY(" + RESERVE_BOOK_ID + ") REFERENCES " + BOOK_TABLE + "(" + BOOK_ID + "), "
                + "FOREIGN KEY(" + RESERVE_MEMBER_NAME + ") REFERENCES " + TABLE_MEMBER + "(" + MEMBER_NAME + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BOOK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + RESERVE_TABLE);
        onCreate(db);
    }

    // -------------------- Utilisateurs --------------------
    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " + EMAIL + "=? AND " + PASSWORD + "=?",
                new String[]{email, password});
        boolean isLoggedIn = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return isLoggedIn;
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ROLE + " FROM " + USER_TABLE + " WHERE " + EMAIL + "=?",
                new String[]{email});
        String role = null;
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(ROLE));
        }
        if (cursor != null) cursor.close();
        db.close();
        return role;
    }

    public void registerUser(String name, String email, String password, String confirmPassword, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(EMAIL, email);
        values.put(PASSWORD, password);
        values.put(CONFIRM_PASSWORD, confirmPassword);
        values.put(ROLE, role);
        db.insert(USER_TABLE, null, values);
        db.close();
    }

    // -------------------- Livres --------------------
    public void addBook(String bookName, String bookAuthor, String bookPublisher, int bookQuantity, boolean bookAvailable) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_NAME, bookName);
        values.put(BOOK_AUTHOR, bookAuthor);
        values.put(BOOK_PUBLISHER, bookPublisher);
        values.put(BOOK_QUANTITY, bookQuantity);
        values.put(BOOK_AVAILABLE, bookAvailable);
        db.insert(BOOK_TABLE, null, values);
        db.close();
    }
/* ==================== LIVRES (DESACTIVE - FIREBASE ONLY) ====================

    public void updateBook(int bookId, String bookName, String bookAuthor, String bookPublisher, int bookQuantity, boolean bookAvailable) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_NAME, bookName);
        values.put(BOOK_AUTHOR, bookAuthor);
        values.put(BOOK_PUBLISHER, bookPublisher);
        values.put(BOOK_QUANTITY, bookQuantity);
        values.put(BOOK_AVAILABLE, bookAvailable);
        db.update(BOOK_TABLE, values, bookName + "=?", new String[]{String.valueOf(bookName)});
        db.close();
    }

    // Supprimer un livre par son nom exact
    public void deleteBookByName(String bookName) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(
                BOOK_TABLE,
                "bname = ?",
                new String[]{bookName}
        );

        db.close();
    }


    public Cursor getAllBook() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + BOOK_TABLE, null);
    }

    public Cursor getBookByName(String bookName) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                BOOK_TABLE,
                null,
                "TRIM(LOWER(bname)) = ?",
                new String[]{bookName.trim().toLowerCase()},
                null,
                null,
                null
        );
    }
=

    // -------------------- Membres --------------------
    public void addMember(String name, String email, String phone, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEMBER_NAME, name);
        values.put(MEMBER_EMAIL, email);
        values.put(MEMBER_PHONE, phone);
        values.put(MEMBER_ADDRESS, address);
        db.insert(TABLE_MEMBER, null, values);
        db.close();
    }

    public Cursor getAllMembers() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MEMBER, null);
    }

    public Cursor getMemberByName(String memberName) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {MEMBER_ID, MEMBER_NAME, MEMBER_EMAIL, MEMBER_PHONE, MEMBER_ADDRESS};
        String selection = MEMBER_NAME + "=?";
        String[] selectionArgs = {memberName};
        return db.query(TABLE_MEMBER, projection, selection, selectionArgs, null, null, null);
    }

    public void updateMember(int memberId, String name, String email, String phone, String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEMBER_NAME, name);
        values.put(MEMBER_EMAIL, email);
        values.put(MEMBER_PHONE, phone);
        values.put(MEMBER_ADDRESS, address);
        db.update(TABLE_MEMBER, values, MEMBER_ID + "=?", new String[]{String.valueOf(memberId)});
        db.close();
    }

    public void deleteMember(int memberId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEMBER, MEMBER_ID + "=?", new String[]{String.valueOf(memberId)});
        db.close();
    }

    // -------------------- Réservations --------------------
    public void reserveBook(int bookId, String memberName) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(BOOK_TABLE, new String[]{BOOK_QUANTITY}, BOOK_ID + "=?", new String[]{String.valueOf(bookId)}, null, null, null);
        int bookQuantity = 0;
        if (cursor != null && cursor.moveToFirst()) {
            bookQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_QUANTITY));
            cursor.close();
        }
        if (bookQuantity > 0) {
            ContentValues bookValues = new ContentValues();
            bookValues.put(BOOK_QUANTITY, bookQuantity - 1);
            db.update(BOOK_TABLE, bookValues, BOOK_ID + "=?", new String[]{String.valueOf(bookId)});

            ContentValues reserveValues = new ContentValues();
            reserveValues.put(RESERVE_BOOK_ID, bookId);
            reserveValues.put(RESERVE_MEMBER_NAME, memberName);
            db.insert(RESERVE_TABLE, null, reserveValues);
        }
        db.close();
    }

    public Cursor getAllReservedBooks() {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT " + BOOK_TABLE + "." + BOOK_NAME + ", "
                + BOOK_TABLE + "." + BOOK_AUTHOR + ", "
                + BOOK_TABLE + "." + BOOK_PUBLISHER + ", "
                + RESERVE_TABLE + "." + RESERVE_MEMBER_NAME
                + " FROM " + BOOK_TABLE
                + " INNER JOIN " + RESERVE_TABLE
                + " ON " + BOOK_TABLE + "." + BOOK_ID + " = " + RESERVE_TABLE + "." + RESERVE_BOOK_ID;
        return db.rawQuery(selectQuery, null);
    }

    public void returnBook(String bookName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(RESERVE_TABLE, RESERVE_BOOK_ID + " = (SELECT " + BOOK_ID + " FROM " + BOOK_TABLE + " WHERE " + BOOK_NAME + " = ?)", new String[]{bookName});
        db.execSQL("UPDATE " + BOOK_TABLE + " SET " + BOOK_QUANTITY + " = " + BOOK_QUANTITY + " + 1 WHERE " + BOOK_NAME + " = ?", new String[]{bookName});
        db.close();
    }
/*
    // -------------------- Suggestions --------------------
    public List<String> getBookSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {BOOK_NAME};
        String selection = BOOK_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%"};
        Cursor cursor = db.query(BOOK_TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                suggestions.add(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_NAME)));
            }
            cursor.close();
        }
        db.close();
        return suggestions;
    }

    // -------------------- Méthodes statiques pour EditBook/EditMember --------------------
    public static String getBookDetails(String detailType) {
        switch (detailType) {
            case "BOOK_ID": return "bid";
            case "BOOK_NAME": return "bname";
            case "BOOK_AUTHOR": return "bauthor";
            case "BOOK_PUBLISHER": return "bpublisher";
            case "BOOK_QUANTITY": return "bquantity";
            case "BOOK_AVAILABLE": return "bavailable";
            default: return null;
        }
    }

    public static String getMemberDetails(String detailType) {
        switch (detailType) {
            case "MEMBER_ID": return "id";
            case "MEMBER_NAME": return "name";
            case "MEMBER_EMAIL": return "email";
            case "MEMBER_PHONE": return "phone";
            case "MEMBER_ADDRESS": return "address";
            default: return null;
        }
    }
=


}
*/
