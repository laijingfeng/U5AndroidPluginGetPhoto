using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class Main : MonoBehaviour
{
    public Button btnCamera;
    public Button btnPhoto;
    public Image img;
    public Text txt;

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
        AddLog("GetPhotoCallback " + fileName);
        if (!string.IsNullOrEmpty(fileName))
        {
            this.StartCoroutine(IE_LoadPhoto(fileName));
        }
    }

    private void AddLog(string str)
    {
        txt.text += "\n" + str;
    }

    private IEnumerator IE_LoadPhoto(string fileName)
    {
        AddLog("IE_LoadPhoto " + fileName);

        string url = "file://" + Application.persistentDataPath + "/" + fileName;

        AddLog("url = " + url);

        WWW www = new WWW(url);

        yield return www;

        if (www.isDone)
        {
            AddLog("isDone");

            if (www.error == null)
            {
                AddLog("size " + www.texture.width + " " + www.texture.height);
                AddLog("set texture");
                Sprite sprite = Sprite.Create(www.texture, new Rect(0, 0, www.texture.width, www.texture.height), new Vector2(0.5f, 0.5f));
                img.sprite = sprite;
            }
            else
            {
                AddLog(www.error);
            }
        }

        www.Dispose();
    }

    private void UnityCallAnroid(string methodName, bool isStatic = false, params object[] args)
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity"))
            {
                if (isStatic)
                {
                    jo.CallStatic(methodName, args);
                }
                else
                {
                    jo.Call(methodName, args);
                }
            }
        }
#endif
    }

    private T UnityCallAnroid<T>(string methodName, bool isStatic = false, params object[] args)
    {
        T ret = default(T);

#if UNITY_ANDROID && !UNITY_EDITOR
        using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity"))
            {
                if (isStatic)
                {
                    ret = jo.CallStatic<T>(methodName, args);
                }
                else
                {
                    ret = jo.Call<T>(methodName, args);
                }
            }
        }
#endif
        return ret;
    }
}