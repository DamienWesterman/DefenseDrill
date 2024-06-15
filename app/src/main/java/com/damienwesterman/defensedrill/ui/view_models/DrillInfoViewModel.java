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

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;

/**
 * TODO Doc comments
 */
public class DrillInfoViewModel extends AndroidViewModel {
    private final MutableLiveData<Drill> currentDrill;
    private final DrillRepository repo;

    public DrillInfoViewModel(Application application) {
        super(application);

        currentDrill = new MutableLiveData<>();
        repo = DrillRepository.getInstance(application);
    }

    public LiveData<Drill> getDrill() {
        return currentDrill;
    }

    public void populateDrill(long drillId) {

    }

    public void populateDrill(long categoryId, long subCategoryId) {

    }
}
