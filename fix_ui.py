import re

file_path = "app/src/main/res/layout/fragment_settings.xml"
with open(file_path, "r") as f:
    content = f.read()

# The new UI block we want to inject
new_ui = """
                <!-- Currency Selection Row -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginTop="16dp">

                    <com.google.android.material.textfield.TextInputLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        style="@style/Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
                        android:hint="Currency">

                        <AutoCompleteTextView
                            android:id="@+id/prefCurrencyDropdown"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="none"
                            android:text="$" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/exchangeRateLayout"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:visibility="gone"
                        android:hint="1$ = ₹ ?">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/prefExchangeRateEdit"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="numberDecimal"
                            android:text="95" />
                    </com.google.android.material.textfield.TextInputLayout>
                </LinearLayout>
"""

# Replace the old TextInputLayout block with the new UI
# Looking for the TextInputLayout that contains "Currency Symbol"
pattern = r'<com\.google\.android\.material\.textfield\.TextInputLayout[^>]*>\s*<com\.google\.android\.material\.textfield\.TextInputEditText\s+android:id="@\+id/prefCurrencyEdit".*?</com\.google\.android\.material\.textfield\.TextInputLayout>'
content = re.sub(pattern, new_ui, content, flags=re.DOTALL)

with open(file_path, "w") as f:
    f.write(content)

print("UI updated successfully!")
