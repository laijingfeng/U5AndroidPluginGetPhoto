using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class Main : MonoBehaviour
{
    public Button btnCamera;
    public Button btnPhoto;
    public RawImage img;

    // Use this for initialization
    void Start()
    {
        btnCamera.onClick.AddListener(() =>
        {
            UnityCallAnroid("PickImage", false, "Camera");
        });

        btnPhoto.onClick.AddListener(() =>
        {
            UnityCallAnroid("PickImage", false, "Photo");
        });
    }

    private void GetPhotoCallback(string fileName)
    {
        if (!string.IsNullOrEmpty(fileName))
        {
            this.StartCoroutine(IE_LoadPhoto(fileName));
        }
    }

    private IEnumerator IE_LoadPhoto(string fileName)
    {
        string url = "file://" + Application.persistentDataPath + "/" + fileName;
        WWW www = new WWW(url);

        yield return www;

        if (www.isDone)
        {
            if (www.error == null)
            {
                img.texture = www.texture;
            }
        }

        www.Dispose();
    }

    private void UnityCallAnroid(string methodName, bool isStatic = false, params object[] args)
    {
        AndroidJavaObject jo = GetAndroidJavaObject;
        if (jo == null)
        {
            return;
        }
        if (isStatic)
        {
            jo.CallStatic(methodName, args);
        }
        else
        {
            jo.Call(methodName, args);
        }
    }

    private T UnityCallAnroid<T>(string methodName, bool isStatic = false, params object[] args)
    {
        T ret = default(T);
        AndroidJavaObject jo = GetAndroidJavaObject;
        if (jo == null)
        {
            return ret;
        }
        if (isStatic)
        {
            ret = jo.CallStatic<T>(methodName, args);
        }
        else
        {
            ret = jo.Call<T>(methodName, args);
        }
        return ret;
    }

#if UNITY_ANDROID && !UNITY_EDITOR
private AndroidJavaObject _androidJavaObject = null;
#endif
    private AndroidJavaObject GetAndroidJavaObject
    {
        get
        {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (_androidJavaObject == null)
        {
            AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            _androidJavaObject = jc.GetStatic<AndroidJavaObject>("currentActivity");
        }
        return _androidJavaObject;
#else
            return null;
#endif
        }
    }
}