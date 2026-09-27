package za.ac.richfield.dijong;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import za.ac.richfield.dijong.data.AppSettings;
import za.ac.richfield.dijong.data.IngredientCategory;
import za.ac.richfield.dijong.data.entity.PantryItem;
import za.ac.richfield.dijong.ui.ExpiryBadge;
import za.ac.richfield.dijong.ui.pantry.PantryViewModel;
import za.ac.richfield.dijong.util.ExpiryDateConverter;
import za.ac.richfield.dijong.util.ExpiryStatus;
import za.ac.richfield.dijong.util.QuantityFormatter;

/**
 * Activity for adding or editing a pantry item. Reads and writes go straight through
 * {@link PantryViewModel}; the caller only needs to pass {@link #EXTRA_ITEM_ID} for edits.
 *
 * <p>It finishes with one of the {@code RESULT_*} codes below so the pantry list can confirm
 * what happened in a snackbar, with Undo where that makes sense.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    /** A new item was saved; the result carries its id in {@link #EXTRA_ITEM_ID}. */
    public static final int RESULT_ADDED = RESULT_FIRST_USER + 1;
    /** An existing item was saved. */
    public static final int RESULT_UPDATED = RESULT_FIRST_USER + 2;
    /**
     * The person tapped Delete. The pantry list does the delete itself, so it can offer Undo
     * from the same place the item disappears.
     */
    public static final int RESULT_DELETE_REQUESTED = RESULT_FIRST_USER + 3;

    private static final long NO_ITEM_ID = -1;
    private static final String OTHER_UNIT_OPTION = "Other";
    private static final List<String> IMPERIAL_UNITS = Arrays.asList("oz", "lb");

    private TextInputLayout tilName;
    private EditText etName;
    private TextInputLayout tilQuantity;
    private EditText etQuantity;
    private TextInputLayout tilUnit;
    private AutoCompleteTextView actvUnit;
    private TextInputLayout tilCustomUnit;
    private EditText etCustomUnit;
    private TextInputLayout tilCategory;
    private AutoCompleteTextView actvCategory;
    private TextInputLayout tilExpiryDate;
    private EditText etExpiryDate;
    private TextView tvExpiryBadge;
    private MaterialButton btnSave;

    private PantryViewModel viewModel;
    private long itemId = NO_ITEM_ID;
    private boolean isEditing = false;
    private boolean fieldsPopulated = false;
    private List<String> unitOptions;
    private IngredientCategory selectedCategory = IngredientCategory.OTHER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        tilName = findViewById(R.id.til_name);
        etName = findViewById(R.id.et_name);
        tilQuantity = findViewById(R.id.til_quantity);
        etQuantity = findViewById(R.id.et_quantity);
        tilUnit = findViewById(R.id.til_unit);
        actvUnit = findViewById(R.id.actv_unit);
        tilCustomUnit = findViewById(R.id.til_custom_unit);
        etCustomUnit = findViewById(R.id.et_custom_unit);
        tilCategory = findViewById(R.id.til_category);
        actvCategory = findViewById(R.id.actv_category);
        tilExpiryDate = findViewById(R.id.til_expiry_date);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        tvExpiryBadge = findViewById(R.id.tv_expiry_badge);
        btnSave = findViewById(R.id.btn_save);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = findViewById(R.id.btn_delete);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID);
        isEditing = itemId != NO_ITEM_ID;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(isEditing ? R.string.title_edit_ingredient : R.string.title_add_ingredient);

        setUpUnitDropdown(null);
        setUpCategoryDropdown();
        showCategory(IngredientCategory.OTHER);
        clearErrorWhenEdited(etName, tilName);
        clearErrorWhenEdited(etQuantity, tilQuantity);
        etExpiryDate.addTextChangedListener(new SimpleTextWatcher(this::onExpiryTextChanged));
        tilExpiryDate.setEndIconOnClickListener(v -> showDatePickerDialog());

        if (isEditing) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> requestDelete());
            viewModel.selectItem(itemId);
            viewModel.getSelectedItem().observe(this, this::populateFields);
        }

        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Metric units always; oz and lb only with "Include imperial" on in Settings, or when the
     * item being edited already uses one, so editing never silently changes its unit.
     */
    private void setUpUnitDropdown(String unitBeingEdited) {
        boolean includeImperial = new AppSettings(this).isImperialUnitsEnabled()
                || IMPERIAL_UNITS.contains(unitBeingEdited);
        unitOptions = new ArrayList<>();
        for (String unit : getResources().getStringArray(R.array.ingredient_units)) {
            if (includeImperial || !IMPERIAL_UNITS.contains(unit)) {
                unitOptions.add(unit);
            }
        }
        actvUnit.setAdapter(new ArrayAdapter<>(this, R.layout.item_unit_dropdown, unitOptions));
        // This is a fixed-choice dropdown, not free-text autocomplete: disabling the key
        // listener stops ArrayAdapter's built-in filtering from narrowing the list down to
        // whatever was previously selected every time the menu is reopened.
        actvUnit.setKeyListener(null);
        actvUnit.setOnItemClickListener((parent, view, position, id) -> {
            clearError(tilUnit);
            setCustomUnitVisible(OTHER_UNIT_OPTION.equals(unitOptions.get(position)));
        });
    }

    private void setUpCategoryDropdown() {
        ArrayAdapter<IngredientCategory> adapter = new ArrayAdapter<IngredientCategory>(
                this, R.layout.item_category_dropdown, IngredientCategory.values()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                IngredientCategory category = getItem(position);
                row.setText(category.labelRes);
                row.setCompoundDrawablesRelativeWithIntrinsicBounds(categoryDot(category), null, null, null);
                return row;
            }
        };
        actvCategory.setAdapter(adapter);
        actvCategory.setKeyListener(null);
        actvCategory.setOnItemClickListener((parent, view, position, id) ->
                showCategory(IngredientCategory.values()[position]));
    }

    private void showCategory(IngredientCategory category) {
        selectedCategory = category;
        actvCategory.setText(getString(category.labelRes), false);
        tilCategory.setStartIconTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, category.foregroundColorRes)));
    }

    private Drawable categoryDot(IngredientCategory category) {
        Drawable dot = DrawableCompat.wrap(
                ContextCompat.getDrawable(this, R.drawable.shape_category_dot).mutate());
        DrawableCompat.setTint(dot, ContextCompat.getColor(this, category.foregroundColorRes));
        return dot;
    }

    private void setCustomUnitVisible(boolean visible) {
        tilCustomUnit.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            etCustomUnit.setText(null);
            clearError(tilCustomUnit);
        }
    }

    private void populateFields(PantryItem item) {
        // Only once: LiveData re-emits after a save, and a rotation must not overwrite edits
        if (item == null || fieldsPopulated) {
            return;
        }
        fieldsPopulated = true;
        etName.setText(item.getName());
        etQuantity.setText(QuantityFormatter.format(item.getQuantity()));
        etExpiryDate.setText(ExpiryDateConverter.formatEpochDay(item.getExpiryDate()));
        showCategory(item.getCategoryEnum());

        String unit = item.getUnit();
        setUpUnitDropdown(unit);
        if (unit != null && unitOptions.contains(unit)) {
            actvUnit.setText(unit, false);
        } else {
            actvUnit.setText(OTHER_UNIT_OPTION, false);
            setCustomUnitVisible(true);
            etCustomUnit.setText(unit);
        }
    }

    private void onExpiryTextChanged() {
        clearError(tilExpiryDate);
        Long epochDay = ExpiryDateConverter.parseToEpochDay(etExpiryDate.getText().toString());
        ExpiryBadge.bind(tvExpiryBadge, ExpiryStatus.of(epochDay), true);
    }

    private void showDatePickerDialog() {
        Long current = ExpiryDateConverter.parseToEpochDay(etExpiryDate.getText().toString());
        LocalDate start = current != null ? LocalDate.ofEpochDay(current) : LocalDate.now();
        new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> etExpiryDate.setText(ExpiryDateConverter.formatEpochDay(
                        LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay())),
                start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth())
                .show();
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
            tilName.setError(getString(R.string.error_name_required));
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
            tilQuantity.setError(getString(R.string.error_quantity_positive));
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
        clearError(tilUnit);

        String expiryDateText = etExpiryDate.getText().toString().trim();
        Long expiryDate = ExpiryDateConverter.parseToEpochDay(expiryDateText);
        if (!ExpiryDateConverter.isBlank(expiryDateText) && expiryDate == null) {
            tilExpiryDate.setError(getString(R.string.error_invalid_date));
            etExpiryDate.requestFocus();
            return;
        }

        PantryItem item = new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiryDate);
        item.setCategory(selectedCategory);

        if (isEditing) {
            item.setId(itemId);
            viewModel.update(item);
            setResult(RESULT_UPDATED);
            finish();
        } else {
            // Wait for the new row's id so the pantry list can offer Undo on "Ingredient added"
            btnSave.setEnabled(false);
            viewModel.insert(item, newId -> {
                setResult(RESULT_ADDED, new Intent().putExtra(EXTRA_ITEM_ID, newId));
                finish();
            });
        }
    }

    private void requestDelete() {
        setResult(RESULT_DELETE_REQUESTED, new Intent().putExtra(EXTRA_ITEM_ID, itemId));
        finish();
    }

    private static void clearErrorWhenEdited(EditText field, TextInputLayout layout) {
        field.addTextChangedListener(new SimpleTextWatcher(() -> clearError(layout)));
    }

    /**
     * Clears the message and gives back the space it took. setError(null) alone leaves the
     * error row reserved, which pushed the fields below down after a fix.
     */
    private static void clearError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    /** A TextWatcher that only cares that the text changed, not how. */
    private static final class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChanged;

        SimpleTextWatcher(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onChanged.run();
        }
    }
}
