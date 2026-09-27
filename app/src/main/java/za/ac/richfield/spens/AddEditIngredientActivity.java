package za.ac.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import za.ac.richfield.smartpantry.model.PantryItem;

import java.util.Calendar;

/**
 * Activity for adding or editing a pantry item.
 * Demonstrates Intent usage for passing data to and from the activity.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_ITEM_NAME = "extra_item_name";
    public static final String EXTRA_ITEM_QUANTITY = "extra_item_quantity";
    public static final String EXTRA_ITEM_UNIT = "extra_item_unit";
    public static final String EXTRA_ITEM_EXPIRY_DATE = "extra_item_expiry_date";

    private EditText etName;
    private EditText etQuantity;
    private EditText etUnit;
    private EditText etExpiryDate;
    private Button btnPickDate;
    private Button btnSave;
    private Button btnCancel;

    private long itemId = -1; // -1 indicates new item, otherwise editing existing
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etUnit = findViewById(R.id.et_unit);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Get extras from intent
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ITEM_ID)) {
            itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1);
            isEditing = true;
            // Populate fields with existing data
            etName.setText(intent.getStringExtra(EXTRA_ITEM_NAME));
            etQuantity.setText(String.valueOf(intent.getDoubleExtra(EXTRA_ITEM_QUANTITY, 0)));
            etUnit.setText(intent.getStringExtra(EXTRA_ITEM_UNIT));
            etExpiryDate.setText(intent.getStringExtra(EXTRA_ITEM_EXPIRY_DATE));
        }

        // Set up date picker button
        btnPickDate.setOnClickListener(v -> showDatePickerDialog());

        // Set up save button
        btnSave.setOnClickListener(v -> saveItem());

        // Set up cancel button
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    // Month is 0-based, so add 1
                    String date = String.format("%04d-%02d-%02d", yearSelected, monthOfYear + 1, dayOfMonth);
                    etExpiryDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveItem() {
        // Validate input
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }

        String quantityStr = etQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError(getString(R.string.error_quantity_positive));
            etQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                etQuantity.setError(getString(R.string.error_quantity_positive));
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.error_quantity_positive));
            etQuantity.requestFocus();
            return;
        }

        String unit = etUnit.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Create PantryItem object
        PantryItem item = new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiryDate.isEmpty() ? null : expiryDate);

        // Save to database via intent extras (we'll return the data and let the fragment handle DB operations)
        // Alternatively, we could do the DB operation here, but to demonstrate Intent, we'll return the data.
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ITEM_NAME, name);
        resultIntent.putExtra(EXTRA_ITEM_QUANTITY, quantity);
        resultIntent.putExtra(EXTRA_ITEM_UNIT, unit);
        resultIntent.putExtra(EXTRA_ITEM_EXPIRY_DATE, expiryDate.isEmpty() ? null : expiryDate);
        if (isEditing) {
            resultIntent.putExtra(EXTRA_ITEM_ID, itemId);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}