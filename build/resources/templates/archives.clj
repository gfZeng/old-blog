[:div
 (for [[date ms] (sort-by key #(compare %2 %1) classes)]
   (list
    [:h4 {:style "margin-top: 28px;"} date] 
    [:ul {:style "list-style: none; margin-top: 7px;"}
     (for [m ms]
       [:li [:a {:href (str "/" (:post-uri m))} (:title m)]])]))]

