package fho.kdvs.home

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.multibindings.IntoMap
import fho.kdvs.R
import fho.kdvs.databinding.FragmentStaffDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.PerFragment
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.StaffEntity
import fho.kdvs.injection.ViewModelKey
import kotlinx.android.synthetic.main.fragment_staff_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Module
abstract class SelectionUiModule {

    @Binds
    abstract fun bindViewModelFactory(vmFactory: KdvsViewModelFactory): ViewModelProvider.Factory

    @PerFragment
    @ContributesAndroidInjector(modules = [(StaffDetailsModule::class)])
    abstract fun contributeStaffDetailsFragment(): StaffDetailsFragment
}

@Module
abstract class StaffDetailsModule: ViewModel() {

    @Binds
    @IntoMap
    @PerFragment
    @ViewModelKey(SharedViewModel::class)
    abstract fun bindViewModel(viewModel: StaffDetailsModule): ViewModel
}

class StaffDetailsFragment : BottomSheetDialogFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: SharedViewModel
    private lateinit var fragmentStaffDetailsBinding: FragmentStaffDetailsBinding

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    // Retrieves the staff member from the arguments bundle. Throws an exception if it doesn't exist.
    private val staffMember: StaffEntity by lazy {
        arguments?.let { StaffDetailsFragmentArgs.fromBundle(it) }?.member
            ?: throw IllegalArgumentException("Should have passed a StaffEntity to StaffDetailsFragment")
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val root = LinearLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        root.setPadding(0,0,0,0)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentStaffDetailsBinding = FragmentStaffDetailsBinding.inflate(inflater, container, false)

        fragmentStaffDetailsBinding.apply {
            staff = staffMember
            vm = viewModel
        }

        fragmentStaffDetailsBinding.lifecycleOwner = this

        return fragmentStaffDetailsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeRoot?.setOnClickListener { this.dismiss() }
    }
}