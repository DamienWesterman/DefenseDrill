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

import android.app.Activity;
import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO Doc comments
 */
public class DrillInfoViewModel extends AndroidViewModel {
    private final MutableLiveData<Drill> currentDrill;
    private final MutableLiveData<List<CategoryEntity>> allCategories;
    private final MutableLiveData<List<SubCategoryEntity>> allSubCategories;
    private final DrillRepository repo;
    private DrillGenerator drillGenerator;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public DrillInfoViewModel(Application application) {
        super(application);

        currentDrill = new MutableLiveData<>();
        allCategories = new MutableLiveData<>();
        allSubCategories = new MutableLiveData<>();
        repo = DrillRepository.getInstance(application);
    }

    public LiveData<Drill> getDrill() {
        return currentDrill;
    }

    public void setDrill(Drill drill) {
        if (null != drill) {
            currentDrill.postValue(drill);
        }
    }

    public void populateDrill(long drillId) {
        executor.execute(() -> currentDrill.postValue(repo.getDrill(drillId)));
    }

    public void populateDrill(long categoryId, long subCategoryId) {
        executor.execute(() -> {
            List<Drill> drills;
            if (Constants.USER_RANDOM_SELECTION == categoryId &&
                    Constants.USER_RANDOM_SELECTION == subCategoryId) {
                drills = repo.getAllDrills();
            } else if (Constants.USER_RANDOM_SELECTION == categoryId) {
                drills = repo.getAllDrillsBySubCategoryId(subCategoryId);
            } else if (Constants.USER_RANDOM_SELECTION == subCategoryId) {
                drills = repo.getAllDrillsByCategoryId(categoryId);
            } else {
                drills = repo.getAllDrills(categoryId, subCategoryId);
            }
            drillGenerator = new DrillGenerator(drills, new Random());
            currentDrill.postValue(drillGenerator.generateDrill());
        });
    }

    public void regenerateDrill() {
        if (null != drillGenerator) {
            currentDrill.setValue(drillGenerator.regenerateDrill());
        }
    }

    public void resetSkippedDrills() {
        if (null != drillGenerator) {
            drillGenerator.resetSkippedDrills();
        }
    }

    /**
     * why we pass in app context
     */
    public void saveDrill(Drill drill, Activity activity) {
        executor.execute(() -> {
           try {
               repo.updateDrills(drill);
               currentDrill.postValue(drill);
               activity.runOnUiThread(() ->
                       Toast.makeText(activity, "Successfully saved changes!",
                               Toast.LENGTH_SHORT).show());
           } catch (SQLiteConstraintException e) {
               activity.runOnUiThread(() ->
                       Toast.makeText(activity, "Issue saving Drill", Toast.LENGTH_SHORT).show());
           }
        });
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<SubCategoryEntity>> getAllSubCategories() {
        return allSubCategories;
    }

    public void loadAllCategories() {
        if (null == allCategories.getValue()) {
            executor.execute(() -> allCategories.postValue(repo.getAllCategories()));
        } else {
            // Force the observer to trigger
            allCategories.setValue(allCategories.getValue());
        }
    }

    public void loadAllSubCategories() {
        if (null == allSubCategories.getValue()) {
            executor.execute(() -> allSubCategories.postValue(repo.getAllSubCategories()));
        } else {
            // Force the observer to trigger
            allSubCategories.setValue(allSubCategories.getValue());
        }
    }
}
