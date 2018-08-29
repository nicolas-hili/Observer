################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CC_SRCS += \
../src/CLIUtils.cc \
../src/Config.cc \
../src/Event.cc \
../src/Method.cc \
../src/Observer.cc \
../src/Serializer.cc \
../src/SharedMem.cc \
../src/Socket.cc \
../src/Text.cc 

CC_DEPS += \
./src/CLIUtils.d \
./src/Config.d \
./src/Event.d \
./src/Method.d \
./src/Observer.d \
./src/Serializer.d \
./src/SharedMem.d \
./src/Socket.d \
./src/Text.d 

OBJS += \
./src/CLIUtils.o \
./src/Config.o \
./src/Event.o \
./src/Method.o \
./src/Observer.o \
./src/Serializer.o \
./src/SharedMem.o \
./src/Socket.o \
./src/Text.o 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cc
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"/Users/mojtababagherzadeh/git/Observer/Observer/ca.queensu.cs.observer.cpp/include" -I"/home/hili/bin/eclipse/Papyrus-RT-1.0/Papyrus-RT/plugins/org.eclipse.papyrusrt.rts_1.0.0.201707181457/umlrts/include" -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


