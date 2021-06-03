package com.example.todo.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import com.example.todo.R
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import com.example.todo.fragments.list.ListFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import org.angmarch.views.NiceSpinner
import java.text.SimpleDateFormat
import java.util.*

class DataBindingAdapters {
    companion object {
        private var lastTouchDown: Long = 0
        private const val CLICK_ACTION_THRESHOLD = 200

        @BindingAdapter("android:navigateToAddFragment")
        @JvmStatic
        fun navigateToAddFragment(
            view: FloatingActionButton,
            navigate: Boolean
        ) {
            view.setOnClickListener {
                if (navigate) {
                    view.findNavController().navigate(R.id.action_listFragment_to_addFragment)
                }
            }
        }


        @BindingAdapter("android:navigateToCheckListFragment")
        @JvmStatic
        fun navigateToCheckListFragment(
            view: FloatingActionButton,
            navigate: Boolean
        ) {
            view.setOnClickListener {
                if (navigate) {
                    view.findNavController()
                        .navigate(R.id.action_listFragment_to_addChecklistFragment)
                }
            }
        }

        @BindingAdapter("android:emptyDatabase")
        @JvmStatic
        fun emptyDatabase(view: View, emptyDatabase: MutableLiveData<Boolean>) {
            when (emptyDatabase.value) {
                true -> {view.visibility = View.VISIBLE
                    val image=view.findViewById<ImageView>(R.id.gif)
                    val animated = AnimatedVectorDrawableCompat.create(image.context, R.drawable.avd_noteit)
                    animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            image.post { animated.start() }
                        }

                    })
                    image.setImageDrawable(animated)
                    animated?.start()

                }
                false -> view.visibility = View.INVISIBLE
            }
        }

        @BindingAdapter("android:getPriorityId")
        @JvmStatic
        fun getPriorityId(v: View, priority: Priority) {
            val context=v.context
            val spinner = v.findViewById(R.id.current_priorities_spinner) as NiceSpinner
            val card = v.findViewById(R.id.priority_indicator) as CardView
            setList(spinner,false)
            spinner.setBackgroundColor(ContextCompat.getColor(context,R.color.primaryBackground))
            when (priority) {
                Priority.HIGH -> {
                    spinner.selectedIndex = 0
                    spinner.setTextColor(ContextCompat.getColor(context, R.color.high))
                    card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.high))
                }
                Priority.LOW -> {
                    spinner.selectedIndex = 2
                    spinner.setTextColor(ContextCompat.getColor(context, R.color.low))
                    card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.low))
                }
                Priority.MEDIUM -> {
                    spinner.selectedIndex = 1
                    spinner.setTextColor(ContextCompat.getColor(context, R.color.medium))
                    card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.medium))
                }
            }
        }


        @BindingAdapter("android:getPriorityColor")
        @JvmStatic
        fun getPriorityColor(priorityIndicator: View, priority: Priority) {
            val context=priorityIndicator.context
            val high=ContextCompat.getColor(context, R.color.high)
            val medium=ContextCompat.getColor(context, R.color.medium)
            val low=ContextCompat.getColor(context, R.color.low)
            when (priorityIndicator) {
                is CardView -> {
                    when (priority) {
                        Priority.HIGH -> priorityIndicator.setCardBackgroundColor(high)
                        Priority.MEDIUM -> priorityIndicator.setCardBackgroundColor(medium)
                        Priority.LOW -> priorityIndicator.setCardBackgroundColor(low)
                    }
                }
                is NiceSpinner -> {
                    setList(priorityIndicator,false)
                    priorityIndicator.setTextAppearance(R.style.textAppearance)
                    when (priority) {
                        Priority.HIGH -> {
                            priorityIndicator.selectedIndex = 0
                            priorityIndicator.setTextColor(high)}
                        Priority.MEDIUM -> {
                            priorityIndicator.selectedIndex = 1
                            priorityIndicator.setTextColor(medium) }
                        Priority.LOW -> {
                            priorityIndicator.selectedIndex = 2
                            priorityIndicator.setTextColor(low) }
                    }

                }
                is TextInputLayout -> {
                    when(priority){
                        Priority.HIGH->{priorityIndicator.boxStrokeColor=high}
                        Priority.MEDIUM -> {priorityIndicator.boxStrokeColor=(medium)}
                        Priority.LOW -> {priorityIndicator.boxStrokeColor=(low)}
                    }
                }
            }

        }

        @BindingAdapter("android.navigateToUpdateFragment")
        @JvmStatic
        fun navigateToUpdateFragment(view: RelativeLayout, currentItem: ToDoData) {
            view.setOnClickListener {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }

        //For Add and Update Fragment
        @BindingAdapter("android:LoadImageWithGlide")
        @JvmStatic
        fun loadImageWithGlide(view: RelativeLayout, path: String) {
            val imageView=view.getChildAt(0) as ImageView
            if (path != "") {
                GlideApp.with(view.context).load(path).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        view.visibility = View.GONE
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(imageView)
                view.visibility = View.VISIBLE

            }
        }


        @BindingAdapter("android:setURL")
        @JvmStatic
        fun setUrl(root: RelativeLayout, url: String) {
            if (url != "") {
                root.visibility = View.VISIBLE
                root.findViewById<TextView>(R.id.url_text).text = url
            }
        }

        //For List Fragment
        @BindingAdapter("android:LoadImage")
        @JvmStatic
        fun loadImage(view: ImageView, path: String) {
            if (path != "") {
               GlideApp.with(view.context).load(path).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        view.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(view)
                view.visibility = View.VISIBLE
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @BindingAdapter("android:navigateToUpdateCheckListFragment")
        @JvmStatic
        fun navigateToUpdateCheckListFragment(view: View, currentList: ToDoData) {
            if (view is RecyclerView) {
                view.setOnTouchListener { p0, p1 ->
                    when (p1?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastTouchDown = System.currentTimeMillis()
                        }
                        MotionEvent.ACTION_UP -> {
                            if (System.currentTimeMillis() - lastTouchDown <= CLICK_ACTION_THRESHOLD) {
                                val bundle = Bundle()
                                bundle.putParcelable("currentList", currentList)
                                view.findNavController().navigate(
                                    R.id.action_listFragment_to_updateCheckListFragment,
                                    bundle
                                )
                            }
                        }
                    }
                    p0?.onTouchEvent(p1) ?: true
                }
            } else {
                view.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putParcelable("currentList", currentList)
                    view.findNavController().navigate(
                        R.id.action_listFragment_to_updateCheckListFragment,
                        bundle
                    )
                }
            }

        }

        @BindingAdapter("android:getCurrentTime")
        @JvmStatic
        fun getCurrentTime(view: TextView, setDate: Boolean) {
            if (setDate) {
                val df = SimpleDateFormat("EEE, d MMM yyyy, h:mm a")
                val date = df.format(Calendar.getInstance().time)
                view.text = date
            }
        }

        @BindingAdapter("android:setList")
        @JvmStatic
        fun setList(view: NiceSpinner, isAddFragment: Boolean) {
            val list = LinkedList<String>()
            for (string in view.context.resources.getStringArray(R.array.priorities))
                list.add(string)
            view.attachDataSource(list)
            if (isAddFragment) {
                view.selectedIndex = 0
                view.setTextColor(ContextCompat.getColor(view.context, R.color.high))
            }
        }

        @BindingAdapter("android:setReminder")
        @JvmStatic
        fun setReminder(view:RelativeLayout,date:String?){
            if(date!=null){
                val tv=view.getChildAt(1) as TextView
                tv.text=date
                view.visibility=View.VISIBLE

            }
        }






        

            


    }


}