map <F2> :call Compile()<CR>
func! Compile()
	if &filetype == 'c'
		exec "w"
		exec "! clear;
		\ echo Compiling % ...;
		\ echo ;
		\ echo gcc % -g -o %<;
		\ gcc % -g -o %<;
		\ echo ;
		\ echo Compiled OK;
		\ echo code path:`pwd`%;
		\ cat %;
		\ echo exec %<;
		\ echo ;
		\./%<;
		\rm %<;"
	endif
	if &filetype == 'cpp'
		exec "w"
		exec "! clear;
		\ echo Compiling % ...;
		\ echo ;
		\ echo g++ % -g -o %<;
		\ g++ % -g -o %<;
		\ echo ;
		\ echo Compiled OK;
		\ echo code path:`pwd`%;
		\ cat %;
		\ echo exec %<;
		\ echo ;
		\./%<;
		\rm %<;"
	endif
endfunc
map <F4> ms:call AddTitle()<cr>'s
function AddTitle()
        call append(0,"/*")
        "call append(1,"#")
        call append(1," * Author : kabir")
        "call append(3,"#")
        call append(2," * Email : zhonghuiping@droi.com")
        "call append(5,"#")
        call append(3," * Last modified : ".strftime("%Y-%m-%d %H:%M"))
        "call append(7,"#")
        call append(4," * Filename : ".expand("%:t"))
        "call append(9,"#")
        call append(5," * Description : ")
        "call append(11,"#")
        call append(6," */")
        echohl WarningMsg | echo "Successful in adding the copyright." | echohl None
endf
	
if has("autocmd") 
	au BufReadPost * if line("'\"") > 1 && line("'\"") <= line("$") | exe "normal! g'\"" | endif 
endif 
