package geekbrains.ru.bdaplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.sql.SQLException;

import geekbrains.ru.bdaplication.db.DataSource;
import geekbrains.ru.bdaplication.room.AppDBRoom;
import geekbrains.ru.bdaplication.room.DataToDo;
import geekbrains.ru.bdaplication.room.DataDao;

public class MainActivity extends AppCompatActivity {

    private NoteAdapter adapter;
    private DataSource dataSource;
    AppDBRoom dbRoom;
    DataDao dataDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbRoom = AppRoom.getInstance().getDatabase();
        dataDao = dbRoom.dataDao();

        //TODO:init db
        dataSource = new DataSource(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = findViewById(R.id.note_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //todo
        adapter = new NoteAdapter(dataSource.getReader());

        adapter.setOnMenuItemClickListener(new NoteAdapter.OnMenuItemClickListener() {
            @Override
            public void onItemEditClick(Note note) {
                editElement(note);
            }

            @Override
            public void onItemDeleteClick(Note note) {
                deleteElement(note);
            }
        });

        adapter.setOnMenuInputTextOnSwipe(new NoteAdapter.OnSwipeInputText() {
            @Override
            public void onSwipeInputText(Note note) {
                //fixme
//                addElement();
                addElementBySwipe();
            }
        });

        recyclerView.setAdapter(adapter);

        //fixme swipe
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
//        touchHelper.startSwipe();

//        touchHelper.startSwipe();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                addElement();
                return true;
            case R.id.menu_clear:
                clearList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearList() {
        dataSource.deleteAll();
        refreshData();
    }

    private void refreshData() {
        dataSource.getReader().refresh();
        adapter.notifyDataSetChanged();
    }

    private void addElement() {



        LayoutInflater factory = LayoutInflater.from(this);
        final View alertView = factory.inflate(R.layout.add_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertView);
        builder.setTitle(R.string.alert_title_add);
        builder.setNegativeButton(R.string.alert_cancel, null);
        builder.setPositiveButton(R.string.menu_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dataSource.add(
                        ((TextView)alertView.findViewById(R.id.editTextNoteTitle)).getText().toString(),
                        ((TextView)alertView.findViewById(R.id.editTextNote)).getText().toString()
                );
                refreshData();
            }
        });
        builder.show();
    }

    private void addElementBySwipe() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertView = factory.inflate(R.layout.input_text, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final AlertDialog alertDialog = builder.show();
        builder.setView(alertView);
        builder.setTitle(R.string.alert_title_add);
        builder.setNegativeButton(R.string.alert_cancel, null);
        builder.setPositiveButton(R.string.menu_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                DataToDo dataToDO = new DataToDo();



                dataDao.insert(dataToDO);



                dataSource.add(
                        ((TextView)alertView.findViewById(R.id.etInputText)).getText().toString(), "ToDo");
//                ((TextView)alertView.findViewById(R.id.etInputText)).setId();
                refreshData();
            }
        });
        // FIXME: 2019-12-25 show keyboard
//        alertView.findViewById(R.id.etInputText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//
//                }
//            }
//        });
        builder.show();
    }

    private void editElement(Note note) {
        dataSource.edit( note, "edit title", "edit desc");
        refreshData();
    }

    private void deleteElement(Note note) {
        dataSource.delete(note);
        refreshData();
    }
}

