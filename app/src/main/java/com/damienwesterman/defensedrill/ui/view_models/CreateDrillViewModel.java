/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.ui.utils.CreateNewDrillCallback;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// TODO doc comments
public class CreateDrillViewModel extends AndroidViewModel {
    private final DrillRepository repo;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public CreateDrillViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
    }

    public void saveDrill(Drill drill, CreateNewDrillCallback callback) throws SQLiteConstraintException {
        executor.execute(() -> {
            try {
                repo.insertDrills(drill);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }
}
