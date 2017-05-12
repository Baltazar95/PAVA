(defvar hash (make-hash-table :test 'equal))

(defun make-constructor (class-name class-attributes)
  `(defun ,(intern (concatenate 'string  "MAKE-" (symbol-name class-name))) (&key ,@class-attributes) (vector ,(symbol-name class-name) ,@class-attributes)))

(defun make-getter (class-name x index)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-" (symbol-name x))) (,class-name) 
       (if (,(intern (concatenate 'string (symbol-name class-name) "?")) ,class-name)
		    (aref ,class-name ,index))))

(defun make-getters (class-name class-attributes)
  (let ((index 0))
    (mapcar
     #'(lambda (x) (make-getter class-name x (incf index))) class-attributes)))

(defun make-isinstance (class-name)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "?")) (x)
     (cond ((equal (aref x 0) ,(symbol-name class-name)) T)
	   ((not (car (gethash (aref x 0) ,hash))) NIL)
	   (T (,(intern (concatenate 'string (symbol-name class-name) "?")) ,#(symbol-name `(caar (gethash (aref x 0) ,hash))))))))

(defun insert-in-hash (class-name inheritance class-attributes)
  `(setf (gethash ,(symbol-name class-name) hash)
	 (append '(,inheritance) ',class-attributes)))

(defun get-inherited-attributes (name)
  (if (not (car (gethash (symbol-name name) hash)))
      (cdr (gethash (symbol-name name) hash))
;;;;;;;;;;; caar serve para irmos buscar só o primeiro elemento da herança
      (append (get-inherited-attributes (caar (gethash (symbol-name name) hash)))
	      (cdr (gethash (symbol-name name) hash)))))
      

(defmacro def-class (class-name &rest class-attributes)
  (if (listp class-name)
      (let ((name (nth 0 class-name)) (inheritance (cdr class-name)) (attributes (append (get-inherited-attributes (cadr class-name)) class-attributes)))
	`(progn ,(make-constructor name attributes)
		,@(make-getters name attributes)
		,(make-isinstance name)
		,(insert-in-hash name inheritance class-attributes)))
      `(progn ,(make-constructor class-name class-attributes)
	      ,@(make-getters class-name class-attributes)
	      ,(make-isinstance class-name)
	      ,(insert-in-hash class-name '() class-attributes))))



