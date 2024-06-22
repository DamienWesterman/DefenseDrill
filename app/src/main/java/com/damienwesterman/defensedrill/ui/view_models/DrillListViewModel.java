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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO Doc comments
 */
public class DrillListViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Drill>> drills;
    private final MutableLiveData<List<CategoryEntity>> allCategories;
    private final MutableLiveData<List<SubCategoryEntity>> allSubCategories;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DrillListViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        drills = new MutableLiveData<>();
        allCategories = new MutableLiveData<>();
        allSubCategories = new MutableLiveData<>();
    }

    public LiveData<List<Drill>> getDrills() {
        return drills;
    }

    public void populateDrills() {
        executor.execute(() -> drills.postValue(repo.getAllDrills()));
    }

    public void populateDrills(long categoryId, long subCategoryId) {
        executor.execute(() -> drills.postValue(repo.getAllDrills(categoryId, subCategoryId)));
    }

    public void populateDrillsByCategory(long categoryId)  {
        executor.execute(() -> drills.postValue(repo.getAllDrillsByCategoryId(categoryId)));
    }

    public void populateDrillsBySubCategory(long subCategoryId) {
        executor.execute(() -> drills.postValue(repo.getAllDrillsBySubCategoryId(subCategoryId)));
    }
}
