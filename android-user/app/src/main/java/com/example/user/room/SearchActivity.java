package com.example.user.room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.RoomAdapter;
import com.example.user.model.Room;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RoomRepository;
import com.example.user.utils.BottomNavHelper;
import com.example.user.utils.Format;
import com.example.user.utils.Ui;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;





public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_TYPE = "room_type";

    private static final String[] SORT_LABELS = {
            "Mới nhất", "Giá thấp → cao", "Giá cao → thấp", "Diện tích lớn nhất"
    };
    private static final String[] SORT_VALUES = {
            "newest", "price_asc", "price_desc", "area_desc"
    };

    private TextInputEditText edtKeyword;
    private ChipGroup chipGroupType;
    private ChipGroup chipGroupPrice;
    private Spinner spinnerDistrict;
    private Spinner spinnerSort;
    private TextView txtResultCount;
    private TextView txtEmpty;
    private LinearProgressIndicator progressBar;
    private BottomNavigationView bottomNavigation;

    private RoomAdapter roomAdapter;
    private final RoomRepository roomRepository = new RoomRepository();
    private final List<String> districts = new ArrayList<>();


    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::search;
    private int requestId = 0;
    private boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Ui.setupHeader(this, "Tìm phòng");
        initViews();
        setupRecyclerView();
        setupFilters();
        BottomNavHelper.setup(this, bottomNavigation, R.id.navSearch);

        applyTypeFromIntent(getIntent());
        ready = true;
        search();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_ROOM_TYPE)) {
            applyTypeFromIntent(intent);
            search();
        }
    }

    private void initViews() {
        edtKeyword = findViewById(R.id.edtKeyword);
        chipGroupType = findViewById(R.id.chipGroupType);
        chipGroupPrice = findViewById(R.id.chipGroupPrice);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerSort = findViewById(R.id.spinnerSort);
        txtResultCount = findViewById(R.id.txtResultCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        RecyclerView rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));

        roomAdapter = new RoomAdapter(new RoomAdapter.Listener() {
            @Override
            public void onRoomClick(Room room) {
                Intent intent = new Intent(SearchActivity.this, RoomDetailActivity.class);
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                roomRepository.setFavorite(room.getId(), room.isFavorite(), new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        Ui.toast(SearchActivity.this, message);
                    }

                    @Override
                    public void onError(String message) {
                        Ui.toast(SearchActivity.this, message);
                        search();
                    }
                });
            }
        });
        rvRooms.setAdapter(roomAdapter);
    }

    private void setupFilters() {

        edtKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, 400);
            }
        });
        edtKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(searchRunnable);
                search();
                return true;
            }
            return false;
        });


        chipGroupType.setOnCheckedStateChangeListener((group, ids) -> search());
        chipGroupPrice.setOnCheckedStateChangeListener((group, ids) -> search());


        spinnerSort.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, SORT_LABELS));
        spinnerSort.setOnItemSelectedListener(simpleListener());


        districts.add("Tất cả khu vực");
        spinnerDistrict.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, districts));
        spinnerDistrict.setOnItemSelectedListener(simpleListener());
    }

    private AdapterView.OnItemSelectedListener simpleListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ready) {
                    search();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    private void applyTypeFromIntent(Intent intent) {
        String type = intent.getStringExtra(EXTRA_ROOM_TYPE);
        if ("ROOM".equals(type)) {
            chipGroupType.check(R.id.chipTypeRoom);
        } else if ("APARTMENT".equals(type)) {
            chipGroupType.check(R.id.chipTypeApartment);
        }
    }


    private void updateDistricts(List<Room> rooms) {
        boolean changed = false;
        for (Room r : rooms) {
            String d = r.getDistrict();
            if (d != null && !d.trim().isEmpty() && !districts.contains(d.trim())) {
                districts.add(d.trim());
                changed = true;
            }
        }
        if (changed) {
            ((ArrayAdapter<?>) spinnerDistrict.getAdapter()).notifyDataSetChanged();
        }
    }



    private void search() {
        String keyword = Ui.text(edtKeyword);

        final int myRequest = ++requestId;
        Ui.visible(progressBar, true);

        roomRepository.getRooms(RoomRepository.query(keyword, 100), new ApiCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                if (myRequest != requestId) {
                    return;
                }
                Ui.visible(progressBar, false);
                updateDistricts(rooms);
                List<Room> result = applyFilters(rooms);
                roomAdapter.setRooms(result);
                txtResultCount.setText("Tìm thấy " + result.size() + " phòng");
                Ui.visible(txtEmpty, result.isEmpty());
            }

            @Override
            public void onError(String message) {
                if (myRequest != requestId) {
                    return;
                }
                Ui.visible(progressBar, false);
                txtResultCount.setText(message);
            }
        });
    }

    private List<Room> applyFilters(List<Room> rooms) {
        int typeId = chipGroupType.getCheckedChipId();
        int priceId = chipGroupPrice.getCheckedChipId();
        double min = 0;
        double max = Double.MAX_VALUE;
        if (priceId == R.id.chipPriceUnder3) {
            max = 3_000_000;
        } else if (priceId == R.id.chipPrice3to5) {
            min = 3_000_000;
            max = 5_000_000;
        } else if (priceId == R.id.chipPrice5to8) {
            min = 5_000_000;
            max = 8_000_000;
        } else if (priceId == R.id.chipPriceOver8) {
            min = 8_000_000;
        }
        int districtPos = spinnerDistrict.getSelectedItemPosition();
        String district = districtPos > 0 && districtPos < districts.size() ? districts.get(districtPos) : null;

        List<Room> out = new ArrayList<>();
        for (Room r : rooms) {
            if (r == null) continue;
            if (typeId == R.id.chipTypeRoom && r.isApartment()) continue;
            if (typeId == R.id.chipTypeApartment && !r.isApartment()) continue;
            if (r.getMonthlyPrice() < min || r.getMonthlyPrice() > max) continue;
            if (district != null && !district.equalsIgnoreCase(Format.orEmpty(r.getDistrict()).trim())) continue;
            out.add(r);
        }

        String sort = SORT_VALUES[Math.max(0, spinnerSort.getSelectedItemPosition())];
        if ("price_asc".equals(sort)) {
            Collections.sort(out, (a, b) -> Double.compare(a.getMonthlyPrice(), b.getMonthlyPrice()));
        } else if ("price_desc".equals(sort)) {
            Collections.sort(out, (a, b) -> Double.compare(b.getMonthlyPrice(), a.getMonthlyPrice()));
        } else if ("area_desc".equals(sort)) {
            Collections.sort(out, (a, b) -> Double.compare(
                    b.getAreaM2() == null ? 0 : b.getAreaM2(),
                    a.getAreaM2() == null ? 0 : a.getAreaM2()));
        }
        return out;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.refresh(bottomNavigation, R.id.navSearch);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }
}
