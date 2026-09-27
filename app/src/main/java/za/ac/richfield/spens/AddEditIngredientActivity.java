package za.ac.richfield.spens;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import za.ac.richfield.spens.data.entity.PantryItem;
import za.ac.richfield.spens.ui.pantry.PantryViewModel;
import za.ac.richfield.spens.util.ExpiryDateConverter;
import za.ac.richfield.spens.util.QuantityFormatter;

/**
 * Activity for adding or editing a pantry item. Reads and writes go straight through
 * {@link PantryViewModel}; the caller only needs to pass {@link #EXTRA_ITEM_ID} for edits.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final long NO_ITEM_ID = -1;
    private static final String OTHER_UNIT_OPTION = "Other";

    private EditText etName;
    private EditText etQuantity;
    private TextInputLayout tilUnit;
    private AutoCompleteTextView actvUnit;
    private TextInputLayout tilCustomUnit;
    private EditText etCustomUnit;
    private EditText etExpiryDate;

    private PantryViewModel viewModel;
    private long itemId = NO_ITEM_ID;
    private boolean isEditing = false;
    private List<String> knownUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        tilUnit = findViewById(R.id.til_unit);
        actvUnit = findViewById(R.id.actv_unit);
        tilCustomUnit = findViewById(R.id.til_custom_unit);
        etCustomUnit = findViewById(R.id.et_custom_unit);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        MaterialButton btnPickDate = findViewById(R.id.btn_pick_date);
        MaterialButton btnSave = findViewById(R.id.btn_save);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);

        setUpUnitDropdown();

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID);
        isEditing = itemId != NO_ITEM_ID;
        if (isEditing) {
            viewModel.selectItem(itemId);
            viewModel.getSelectedItem().observe(this, this::populateFields);
        }

        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setUpUnitDropdown() {
        String[] units = getResources().getStringArray(R.array.ingredient_units);
        knownUnits = Arrays.asList(units);
        actvUnit.setAdapter(new ArrayAdapter<>(this, R.layout.item_unit_dropdown, units));
        // This is a fixed-choice dropdown, not free-text autocomplete: disabling the key
        // listener stops ArrayAdapter's built-in filtering from narrowing the list down to
        // whatever was previously selected every time the menu is reopened.
        actvUnit.setKeyListener(null);
        actvUnit.setOnItemClickListener((parent, view, position, id) ->
                setCustomUnitVisible(OTHER_UNIT_OPTION.equals(units[position])));
    }

    private void setCustomUnitVisible(boolean visible) {
        tilCustomUnit.setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
        if (!visible) {
            etCustomUnit.setText(null);
            tilCustomUnit.setError(null);
        }
    }

    private void populateFields(PantryItem item) {
        if (item == null) {
            return;
        }
        etName.setText(item.getName());
        etQuantity.setText(QuantityFormatter.format(item.getQuantity()));
        etExpiryDate.setText(ExpiryDateConverter.formatEpochDay(item.getExpiryDate()));

        String unit = item.getUnit();
        boolean isKnownUnit = unit != null && knownUnits.contains(unit);
        if (isKnownUnit) {
            actvUnit.setText(unit, false);
        } else {
            actvUnit.setText(OTHER_UNIT_OPTION, false);
            setCustomUnitVisible(true);
            etCustomUnit.setText(unit);
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) ->
                        etExpiryDate.setText(LocalDate.of(yearSelected, monthOfYear + 1, dayOfMonth).toString()),
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * @return the chosen unit, or null if nothing was picked, or "Other" was picked but no
     * custom unit was entered.
     */
    private String resolveUnit() {
        String selected = actvUnit.getText().toString().trim();
        if (selected.isEmpty()) {
            return null;
        }
        if (!OTHER_UNIT_OPTION.equals(selected)) {
            return selected;
        }
        String custom = etCustomUnit.getText().toString().trim();
        return custom.isEmpty() ? null : custom;
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }

        String quantityStr = etQuantity.getText().toString().trim();
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Quantity must be positive");
            }
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.error_quantity_positive));
            etQuantity.requestFocus();
            return;
        }

        String unit = resolveUnit();
        if (unit == null) {
            boolean isOtherSelected = OTHER_UNIT_OPTION.equals(actvUnit.getText().toString().trim());
            if (isOtherSelected) {
                tilCustomUnit.setError(getString(R.string.error_custom_unit_required));
                etCustomUnit.requestFocus();
            } else {
                tilUnit.setError(getString(R.string.error_unit_required));
                actvUnit.requestFocus();
                actvUnit.showDropDown();
            }
            return;
        }
        tilUnit.setError(null);

        String expiryDateText = etExpiryDate.getText().toString().trim();
        Long expiryDate = ExpiryDateConverter.parseToEpochDay(expiryDateText);
        if (!ExpiryDateConverter.isBlank(expiryDateText) && expiryDate == null) {
            etExpiryDate.setError(getString(R.string.error_invalid_date));
            etExpiryDate.requestFocus();
            return;
        }

        PantryItem item = new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiryDate);

        if (isEditing) {
            item.setId(itemId);
            viewModel.update(item);
            Toast.makeText(this, R.string.msg_ingredient_updated, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insert(item);
            Toast.makeText(this, R.string.msg_ingredient_added, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
