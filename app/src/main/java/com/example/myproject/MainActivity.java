package com.example.myproject;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Intent;
import android.widget.ScrollView;
import java.security.MessageDigest;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    EditText inputText, keyInput;
    Button encodeButton, decodeButton, clearButton, getOutputButton, copyOutputButton, themeButton, openHistoryButton;
    Spinner algorithmSpinner;
    String selectedAlgorithm = "Base64";

    TextView outputText;

    private static final String AES_KEY = "1234567812345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        inputText = findViewById(R.id.inputText);
        keyInput = findViewById(R.id.keyInput);
        outputText = findViewById(R.id.outputText);
        encodeButton = findViewById(R.id.encodeButton);
        decodeButton = findViewById(R.id.decodeButton);
        clearButton = findViewById(R.id.clearButton);
        getOutputButton = findViewById(R.id.getOutputButton);
        copyOutputButton = findViewById(R.id.copyOutputButton);
        themeButton = findViewById(R.id.themeButton);
        openHistoryButton = findViewById(R.id.openHistoryButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        ScrollView rootLayout = findViewById(R.id.rootScroll);


                outputText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                copyOutputButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        copyOutputButton.setOnClickListener(v -> {
            String result = outputText.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Output", result);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Output copied to clipboard 📋", Toast.LENGTH_SHORT).show();
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Base64", "Caesar Cipher", "AES", "Reverse Cipher", "SHA-256", "Vigenère Cipher"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAlgorithm = parent.getItemAtPosition(position).toString();
                keyInput.setVisibility(selectedAlgorithm.equals("Vigenère Cipher") ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        encodeButton.setOnClickListener(v -> {

            if (selectedAlgorithm.isEmpty()) {
                Toast.makeText(this, "Please select an algorithm!", Toast.LENGTH_SHORT).show();
                return;
            }

            String input = inputText.getText().toString();
            if (input.isEmpty()) {
                inputText.setError("Please enter some text!");
                return;
            }

            String encoded = "";
            switch (selectedAlgorithm) {
                case "Base64":
                    encoded = Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
                    break;
                case "Caesar Cipher":
                    encoded = caesarEncrypt(input, 3);
                    break;
                case "AES":
                    encoded = aesEncrypt(input);
                    break;
                case "Reverse Cipher":
                    encoded = reverseEncrypt(input);
                    break;
                case "SHA-256":
                    encoded = sha256Hash(input);
                    break;
                case "Vigenère Cipher":
                    String key = keyInput.getText().toString();
                    if (key.isEmpty()) {
                        keyInput.setError("Enter key!");
                        return;
                    }
                    encoded = vigenereEncrypt(input, key);
                    break;
            }
            outputText.setText(encoded);
            Toast.makeText(MainActivity.this, "Encoded using " + selectedAlgorithm, Toast.LENGTH_SHORT).show();

            String logEntry = "🔹 Input: " + input + "\n"
                    + "🔹 Algorithm: " + selectedAlgorithm + "\n"
                    + "🔹 Operation: Encryption\n"
                    + "🔹 Output: " + encoded + "\n"
                    + "🔹 Time: " + new Date().toString();
            HistoryActivity.historyList.add(0, logEntry);

        });

        decodeButton.setOnClickListener(v -> {
            String input = inputText.getText().toString();
            if (input.isEmpty()) {
                inputText.setError("Please enter some text!");
                return;
            }

            String decoded = "";
            switch (selectedAlgorithm) {
                case "Base64":
                    try {
                        decoded = new String(Base64.decode(input, Base64.DEFAULT));
                    } catch (Exception e) {
                        decoded = "Invalid Base64 input";
                    }
                    break;
                case "Caesar Cipher":
                    decoded = caesarDecrypt(input, 3);
                    break;
                case "AES":
                    decoded = aesDecrypt(input);
                    break;
                case "Reverse Cipher":
                    decoded = reverseDecrypt(input);
                    break;
                case "SHA-256":
                    decoded = "SHA-256 cannot be decrypted!";
                    break;
                case "Vigenère Cipher":
                    String key = keyInput.getText().toString();
                    if (key.isEmpty()) {
                        keyInput.setError("Enter key!");
                        return;
                    }
                    decoded = vigenereDecrypt(input, key);
                    break;
            }
            outputText.setText(decoded);
            Toast.makeText(MainActivity.this, "Decoded using " + selectedAlgorithm, Toast.LENGTH_SHORT).show();

            String logEntry = "🔹 Input: " + input + "\n"
                    + "🔹 Algorithm: " + selectedAlgorithm + "\n"
                    + "🔹 Operation: Decryption\n"
                    + "🔹 Output: " + decoded + "\n"
                    + "🔹 Time: " + new Date().toString();
            HistoryActivity.historyList.add(0, logEntry);

        });

        clearButton.setOnClickListener(v -> {
            inputText.setText("");
            outputText.setText("");
            keyInput.setText("");
        });

        getOutputButton.setOnClickListener(v -> {
            String result = outputText.getText().toString();
            Toast.makeText(MainActivity.this, "Output: " + result, Toast.LENGTH_LONG).show();
        });

        themeButton.setOnClickListener(v -> {
            currentTheme[0] = (currentTheme[0] + 1) % themeColors.length;
            rootLayout.setBackgroundColor(themeColors[currentTheme[0]]);
        });

        openHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    int[] themeColors = {
            0xFFF8F8FF,
            0xFFE0F2F1,
            0xFFFFFDE7
    };
    final int[] currentTheme = {0};

    private String caesarEncrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) result.append((char) (c + shift));
        return result.toString();
    }

    private String caesarDecrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) result.append((char) (c - shift));
        return result.toString();
    }

    private String aesEncrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            return "AES encryption error!";
        }
    }

    private String aesDecrypt(String encryptedText) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.decode(encryptedText, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            return "AES decryption error!";
        }
    }

    private String reverseEncrypt(String text) {
        return new StringBuilder(text).reverse().toString();
    }

    private String reverseDecrypt(String text) {
        return new StringBuilder(text).reverse().toString();
    }

    private String sha256Hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return "SHA-256 error!";
        }
    }

    private String vigenereEncrypt(String text, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char k = key.charAt(i % key.length());
            result.append((char) ((c + k) % 256));
        }
        return Base64.encodeToString(result.toString().getBytes(), Base64.DEFAULT);
    }

    private String vigenereDecrypt(String base64Text, String key) {
        try {
            String text = new String(Base64.decode(base64Text, Base64.DEFAULT));
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                char k = key.charAt(i % key.length());
                result.append((char) ((c - k + 256) % 256));
            }
            return result.toString();
        } catch (Exception e) {
            return "Vigenère error!";
        }
    }
}
