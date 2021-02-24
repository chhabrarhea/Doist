package com.example.todo.Utils

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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.todo.R
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import com.example.todo.fragments.list.ListFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.angmarch.views.NiceSpinner
import java.text.SimpleDateFormat
import java.util.*

class DataBindingAdapters {


    companion object {
        private var lastTouchDown: Long = 0
        private val CLICK_ACTION_THRESHOLD = 200

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
                true -> view.visibility = View.VISIBLE
                false -> view.visibility = View.INVISIBLE
            }
        }

        @BindingAdapter("android:getPriorityId")
        @JvmStatic
        fun getPriorityId(v: RelativeLayout, priority: Priority) {
            val spinner = v.findViewById(R.id.current_priorities_spinner) as NiceSpinner
            val card = v.findViewById(R.id.priority_indicator) as CardView
            setList(spinner,false)
            when (priority) {
                Priority.HIGH -> {
                    spinner.selectedIndex = 0
                    spinner.setTextColor(ContextCompat.getColor(v.context, R.color.high))
                    card.setCardBackgroundColor(ContextCompat.getColor(v.context, R.color.high))
                }
                Priority.LOW -> {
                    spinner.selectedIndex = 2
                    spinner.setTextColor(ContextCompat.getColor(v.context, R.color.low))
                    card.setCardBackgroundColor(ContextCompat.getColor(v.context, R.color.low))
                }
                Priority.MEDIUM -> {
                    spinner.selectedIndex = 1
                    spinner.setTextColor(ContextCompat.getColor(v.context, R.color.medium))
                    card.setCardBackgroundColor(ContextCompat.getColor(v.context, R.color.medium))
                }
            }
        }


        @BindingAdapter("android:getPriorityColor")
        @JvmStatic
        fun getPriorityColor(priorityIndicator: View, priority: Priority) {
            if (priorityIndicator is CardView) {
                when (priority) {
                    Priority.HIGH -> priorityIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(
                            priorityIndicator.context,
                            R.color.high
                        )
                    )
                    Priority.MEDIUM -> priorityIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(
                            priorityIndicator.context,
                            R.color.medium
                        )
                    )
                    Priority.LOW -> priorityIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(
                            priorityIndicator.context,
                            R.color.low
                        )
                    )
                }
            } else if (priorityIndicator is NiceSpinner) {
                Log.i("priority",priority.name)
                priorityIndicator.setTextAppearance(R.style.textAppearance)
                when (priority) {
                    Priority.HIGH -> {
                        Log.i("priority","high")
                        priorityIndicator.setTextColor(
                            ContextCompat.getColor(
                                priorityIndicator.context,
                                R.color.high
                            )
                        )
                        priorityIndicator.selectedIndex = 0
                    }
                    Priority.MEDIUM -> {
                        Log.i("priority","medium")
                        priorityIndicator.setTextColor(
                            ContextCompat.getColor(
                                priorityIndicator.context,
                                R.color.medium
                            )
                        )
                        priorityIndicator.selectedIndex = 1
                    }
                    Priority.LOW -> {
                        Log.i("priority","low")
                        priorityIndicator.setTextColor(
                            ContextCompat.getColor(
                                priorityIndicator.context,
                                R.color.low
                            )
                        )
                        priorityIndicator.selectedIndex = 2
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
            val imageView=view.findViewById(R.id.image) as ImageView
            if (!path.equals("")) {
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
            Log.i("glide", path)
            if (!path.equals("")) {
                GlideApp.with(view.context).load(path).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.i("Glide", e.toString() + " j")
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

        @BindingAdapter("android:navigateToUpdateCheckListFragment")
        @JvmStatic
        fun navigateToUpdateCheckListFragment(view: View, currentList: ToDoData) {
            if (view is RecyclerView) {
                view.setOnTouchListener(object : View.OnTouchListener {
                    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
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
                        return p0?.onTouchEvent(p1) ?: true
                    }

                })
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

    }


}