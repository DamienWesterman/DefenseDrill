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
    private List<CategoryEntity> allCategories;
    private List<SubCategoryEntity> allSubCategories;
    private final DrillRepository repo;
    private DrillGenerator drillGenerator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DrillInfoViewModel(Application application) {
        super(application);

        currentDrill = new MutableLiveData<>();
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
     * why we pass in app context, may be null if no toast to show
     */
    public void saveDrill(Drill drill, Activity activity) {
        if (null == drill) {
            if (null != activity) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Issue saving Drill", Toast.LENGTH_SHORT).show());
            }
            return;
        }
        executor.execute(() -> {
           try {
               repo.updateDrills(drill);
               currentDrill.postValue(drill);
               if (null != activity) {
                   activity.runOnUiThread(() ->
                           Toast.makeText(activity, "Successfully saved changes!",
                                   Toast.LENGTH_SHORT).show());
               }
           } catch (SQLiteConstraintException e) {
               if (null != activity) {
                   activity.runOnUiThread(() ->
                           Toast.makeText(activity, "Issue saving Drill", Toast.LENGTH_SHORT).show());
               }
           }
        });
    }

    public List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    public List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    public void loadAllCategories() {
        executor.execute(() -> allCategories = repo.getAllCategories());
    }

    public void loadAllSubCategories() {
            executor.execute(() -> allSubCategories = repo.getAllSubCategories());
    }
}
