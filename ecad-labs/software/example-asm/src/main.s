.text
myfunction:
	addi sp,sp,-32	# Allocate stack space
	
	# store any callee-saved register you might overwrite
	sw ra, 0(sp)
	
	# do your work
	
	# load every register you stored above
	lw ra, 0(sp)
	addi sp,sp,32 	# Free up stack space
	ret

.global main		# Export the symbol 'main' so we can call it from other files
.type main, @function
main:
	addi sp,sp,-32 	# Allocate stack space
	sw ra, 0(sp)
	call myfunction # and jump to a function
	lw ra, 0(sp)
	addi sp,sp,32 	# Free up stack space
	ret
