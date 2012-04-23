namespace java org.styloot.capri.gen

typedef string ItemId

service Capri {
  /*
   The bytes red, green and blue are signed - basically ordinary rgb values - 128, range is [-128, 127]

   If colorDist is negative, no color filter will be applied.
   */
  list<ItemId> findWithText(1:string category_name, 2:list<string> features, 3:byte red, 4:byte green, 5:byte blue, 6:double colorDist, 7:i32 cost_min, 8:i32 cost_max, 9:string text,10:i32 page),

  /*
       Keeping the old interface too.
  */
  list<ItemId> find(1:string category_name, 2:list<string> features, 3:byte red, 4:byte green, 5:byte blue, 6:double colorDist, 7:i32 cost_min, 8:i32 cost_max, 9:i32 page),
}
